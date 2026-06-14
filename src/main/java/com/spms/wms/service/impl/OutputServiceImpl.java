package com.spms.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseEntity;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import com.spms.wms.entity.InventoryEntity;
import com.spms.wms.entity.OutputDetailEntity;
import com.spms.wms.entity.OutputEntity;
import com.spms.wms.enums.OutputStatus;
import com.spms.wms.enums.OutputType;
import com.spms.wms.mapper.InventoryMapper;
import com.spms.wms.mapper.OutputDetailMapper;
import com.spms.wms.mapper.OutputMapper;
import com.spms.wms.model.OutputFinishRequest;
import com.spms.wms.model.OutputPageFilter;
import com.spms.wms.service.OutputService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class OutputServiceImpl extends BaseService<OutputEntity> implements OutputService {
    private final OutputMapper outputMapper;
    private final OutputDetailMapper outputDetailMapper;
    private final InventoryMapper inventoryMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<OutputEntity> getPage(PageQuery<OutputPageFilter> request) {
        OutputPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("billCode", OutputPageFilter::billCode)
                .put("status", OutputPageFilter::status)
                .put("type", OutputPageFilter::type)
                .put("saleId", OutputPageFilter::saleId)
                .put("moveId", OutputPageFilter::moveId)
                .put("pickingId", OutputPageFilter::pickingId)
                .toMap();
        Page<OutputEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(outputMapper.getPageList(page, params), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(OutputEntity request) {
        validateOutput(request, false);

        OutputEntity output = new OutputEntity();
        BeanUtils.copyProperties(request, output);
        output.setBillCode(resolveBillCode(request.getBillCode()))
                .setStatus(OutputStatus.AUDITING.getValue())
                .setType(request.getType() == null ? OutputType.NORMAL.getValue() : request.getType());
        initAddEntity(output);
        outputMapper.insert(output);

        saveDetails(output.getId(), request.getDetails());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OutputEntity request) {
        validateOutput(request, true);
        OutputEntity exist = getRequiredOutput(request.getId());
        if (OutputStatus.OUTPUTTING.getValue().equals(exist.getStatus())
                || OutputStatus.FINISHED.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前出库单状态无法修改");
        }

        outputDetailMapper.delete(Wrappers.<OutputDetailEntity>lambdaQuery()
                .eq(OutputDetailEntity::getBillId, request.getId()));

        OutputEntity output = new OutputEntity();
        BeanUtils.copyProperties(request, output);
        output.setBillCode(resolveUpdateBillCode(request.getBillCode(), exist.getBillCode()))
                .setStatus(OutputStatus.AUDITING.getValue())
                .setType(request.getType() == null ? OutputType.NORMAL.getValue() : request.getType());
        initUpdateEntity(output);
        outputMapper.updateById(output);

        saveDetails(output.getId(), request.getDetails());
    }

    @Override
    public OutputEntity getDetail(Map<String, Object> request) {
        Long id = getId(request);
        OutputEntity output = getRequiredOutput(id);
        output.setDetails(outputDetailMapper.getByBillId(output.getId()));
        return output;
    }

    @Override
    public void audit(OutputEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        OutputEntity output = getRequiredOutput(request.getId());
        if (!OutputStatus.AUDITING.getValue().equals(output.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
        }
        OutputEntity update = new OutputEntity();
        update.setId(request.getId());
        update.setStatus(OutputStatus.OUTPUTTING.getValue());
        initUpdateEntity(update);
        outputMapper.updateById(update);
    }

    @Override
    public void reject(OutputEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        requireText(request.getRejectReason(), "驳回原因不能为空");
        OutputEntity output = getRequiredOutput(request.getId());
        if (!OutputStatus.AUDITING.getValue().equals(output.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法驳回");
        }
        OutputEntity update = new OutputEntity();
        update.setId(request.getId());
        update.setStatus(OutputStatus.REJECTED.getValue());
        update.setRejectReason(request.getRejectReason());
        initUpdateEntity(update);
        outputMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFinish(OutputFinishRequest request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.id(), "出库明细ID不能为空");
        BigDecimal quantity = requirePositiveQuantity(request.quantity(), "本次出库数量必须大于0");

        OutputDetailEntity detail = outputDetailMapper.selectById(request.id());
        if (detail == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "出库明细不存在");
        }
        OutputEntity output = getRequiredOutput(detail.getBillId());
        if (!OutputStatus.OUTPUTTING.getValue().equals(output.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前出库单状态无法执行出库");
        }

        BigDecimal targetQuantity = requirePositiveQuantity(detail.getQuantity(), "出库明细数量配置不正确");
        BigDecimal oldFinishQuantity = BigDecimal.valueOf(detail.getFinishQuantity() == null ? 0D : detail.getFinishQuantity());
        BigDecimal newFinishQuantity = oldFinishQuantity.add(quantity);
        if (newFinishQuantity.compareTo(targetQuantity) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "累计出库数量不能超过明细数量");
        }

        decreaseInventory(detail.getInventoryId(), detail.getMaterialId(), quantity);

        detail.setFinishQuantity(newFinishQuantity.doubleValue())
                .setIsFinished(newFinishQuantity.compareTo(targetQuantity) >= 0);
        initUpdateBaseEntity(detail);
        outputDetailMapper.updateById(detail);

        finishOutputIfAllDetailsFinished(output.getId());
    }

    private void validateOutput(OutputEntity output, boolean requireId) {
        requireNotNull(output, "请求参数不能为空");
        if (requireId) {
            requireId(output.getId(), "出库单ID不能为空");
        }
        List<OutputDetailEntity> details = output.getDetails();
        requireNotNull(details, "出库明细不能为空");
        if (details.isEmpty()) {
            throw new AppException(CommonError.PARAM_MISSING, "出库明细不能为空");
        }
        for (OutputDetailEntity detail : details) {
            requireNotNull(detail, "出库明细不能为空");
            Long inventoryId = getInventoryId(detail);
            Long materialId = getMaterialId(detail);
            requireId(inventoryId, "来源库存不能为空");
            requireId(materialId, "出库物料不能为空");
            InventoryEntity inventory = getRequiredInventory(inventoryId);
            if (!Objects.equals(inventory.getMaterialId(), materialId)) {
                throw new AppException(CommonError.PARAM_INVALID, "来源库存物料与出库物料不一致");
            }
            requirePositiveQuantity(detail.getQuantity(), "出库数量必须大于0");
        }
    }

    private void saveDetails(Long billId, List<OutputDetailEntity> details) {
        for (OutputDetailEntity detail : details) {
            Long inventoryId = getInventoryId(detail);
            Long materialId = getMaterialId(detail);
            OutputDetailEntity entity = new OutputDetailEntity();
            entity.setBillId(billId)
                    .setInventoryId(inventoryId)
                    .setMaterialId(materialId)
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(0D)
                    .setIsFinished(false);
            initAddBaseEntity(entity);
            outputDetailMapper.insert(entity);
        }
    }

    private void decreaseInventory(Long inventoryId, Long materialId, BigDecimal quantity) {
        InventoryEntity inventory = getRequiredInventory(inventoryId);
        if (!Objects.equals(inventory.getMaterialId(), materialId)) {
            throw new AppException(CommonError.PARAM_INVALID, "来源库存物料与出库物料不一致");
        }
        BigDecimal oldQuantity = BigDecimal.valueOf(inventory.getQuantity() == null ? 0D : inventory.getQuantity());
        BigDecimal newQuantity = oldQuantity.subtract(quantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(CommonError.PARAM_INVALID, "库存数量不足");
        }
        inventory.setQuantity(newQuantity.doubleValue());
        initUpdateBaseEntity(inventory);
        inventoryMapper.updateById(inventory);
    }

    private void finishOutputIfAllDetailsFinished(Long outputId) {
        List<OutputDetailEntity> details = outputDetailMapper.selectList(Wrappers.<OutputDetailEntity>lambdaQuery()
                .eq(OutputDetailEntity::getBillId, outputId));
        boolean allFinished = !details.isEmpty()
                && details.stream().allMatch(detail -> Boolean.TRUE.equals(detail.getIsFinished()));
        if (!allFinished) {
            return;
        }
        OutputEntity update = new OutputEntity();
        update.setId(outputId);
        update.setStatus(OutputStatus.FINISHED.getValue());
        initUpdateEntity(update);
        outputMapper.updateById(update);
    }

    private OutputEntity getRequiredOutput(Long id) {
        requireId(id, "出库单ID不能为空");
        OutputEntity output = outputMapper.getById(id);
        if (output == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "出库单不存在");
        }
        return output;
    }

    private InventoryEntity getRequiredInventory(Long id) {
        requireId(id, "来源库存不能为空");
        InventoryEntity inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "来源库存不存在");
        }
        return inventory;
    }

    private Long getInventoryId(OutputDetailEntity detail) {
        if (detail.getInventoryId() != null) {
            return detail.getInventoryId();
        }
        if (detail.getInventory() != null) {
            return detail.getInventory().getId();
        }
        return null;
    }

    private Long getMaterialId(OutputDetailEntity detail) {
        if (detail.getMaterialId() != null) {
            return detail.getMaterialId();
        }
        if (detail.getMaterial() != null) {
            return detail.getMaterial().getId();
        }
        return null;
    }

    private Long getId(Map<String, Object> request) {
        requireNotNull(request, "请求参数不能为空");
        Object value = request.get("id");
        if (value == null) {
            throw new AppException(CommonError.PARAM_MISSING, "id不能为空");
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            throw new AppException(CommonError.PARAM_INVALID, "id格式不正确");
        }
    }

    private String resolveBillCode(String billCode) {
        String code = trimToNull(billCode);
        if (code != null) {
            return code;
        }
        return codeRuleService.createCode(CodeRuleField.OUTPUT_BILL_CODE);
    }

    private String resolveUpdateBillCode(String requestBillCode, String existBillCode) {
        String code = trimToNull(requestBillCode);
        if (code != null) {
            return code;
        }
        return resolveBillCode(existBillCode);
    }

    private BigDecimal requirePositiveQuantity(Double value, String message) {
        requireNotNull(value, message);
        BigDecimal quantity = BigDecimal.valueOf(value);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(CommonError.PARAM_INVALID, message);
        }
        return quantity;
    }

    private void initAddBaseEntity(BaseEntity entity) {
        long now = System.currentTimeMillis();
        entity.setId(null);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDisabled(Boolean.TRUE.equals(entity.getIsDisabled()));
        entity.setIsPublished(false);
    }

    private void initUpdateBaseEntity(BaseEntity entity) {
        entity.setUpdateTime(System.currentTimeMillis());
    }
}
