package com.spms.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.IdRequest;
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
import com.spms.wms.model.OutputAddRequest;
import com.spms.wms.model.OutputFinishRequest;
import com.spms.wms.model.OutputUpdateRequest;
import com.spms.wms.model.OutputPageFilter;
import com.spms.wms.service.OutputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spms.base.RejectRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requirePositiveQuantity;
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
    public void add(OutputAddRequest request) {
        OutputEntity output = new OutputEntity();
        output.setBillCode(resolveBillCode(request.billCode()))
                .setStatus(OutputStatus.AUDITING.getValue())
                .setType(request.type() == null ? OutputType.NORMAL.getValue() : request.type())
                .setMoveId(request.moveId())
                .setPickingId(request.pickingId())
                .setSaleId(request.saleId());
        outputMapper.insert(output);

        saveDetails(output.getId(), request.details());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OutputUpdateRequest request) {
        OutputEntity exist = getRequiredOutput(request.id());
        if (OutputStatus.OUTPUTTING.getValue().equals(exist.getStatus())
                || OutputStatus.FINISHED.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前出库单状态无法修改");
        }

        outputDetailMapper.delete(Wrappers.<OutputDetailEntity>lambdaQuery()
                .eq(OutputDetailEntity::getBillId, request.id()));

        OutputEntity output = new OutputEntity();
        output.setId(request.id());
        output.setBillCode(resolveUpdateBillCode(request.billCode(), exist.getBillCode()))
                .setStatus(OutputStatus.AUDITING.getValue())
                .setType(request.type() == null ? OutputType.NORMAL.getValue() : request.type())
                .setMoveId(request.moveId())
                .setPickingId(request.pickingId())
                .setSaleId(request.saleId());
        outputMapper.updateById(output);

        saveDetails(output.getId(), request.details());
    }

    @Override
    public OutputEntity getDetail(IdRequest request) {
        OutputEntity output = getRequiredOutput(request.id());
        output.setDetails(outputDetailMapper.getByBillId(output.getId()));
        return output;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(IdRequest request) {
        OutputEntity output = getRequiredOutput(request.id());
        if (!OutputStatus.AUDITING.getValue().equals(output.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
        }
        OutputEntity update = new OutputEntity();
        update.setId(request.id());
        update.setStatus(OutputStatus.OUTPUTTING.getValue());
        outputMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(RejectRequest request) {
        OutputEntity output = getRequiredOutput(request.id());
        if (!OutputStatus.AUDITING.getValue().equals(output.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法驳回");
        }
        OutputEntity update = new OutputEntity();
        update.setId(request.id());
        update.setStatus(OutputStatus.REJECTED.getValue());
        update.setRejectReason(request.rejectReason());
        outputMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFinish(OutputFinishRequest request) {
        BigDecimal quantity = request.quantity();

        OutputDetailEntity detail = outputDetailMapper.selectById(request.id());
        if (detail == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "出库明细不存在");
        }
        OutputEntity output = getRequiredOutput(detail.getBillId());
        if (!OutputStatus.OUTPUTTING.getValue().equals(output.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前出库单状态无法执行出库");
        }

        BigDecimal targetQuantity = requirePositiveQuantity(detail.getQuantity(), "出库明细数量配置不正确");
        BigDecimal oldFinishQuantity = detail.getFinishQuantity() == null ? BigDecimal.ZERO : detail.getFinishQuantity();
        BigDecimal newFinishQuantity = oldFinishQuantity.add(quantity);
        if (newFinishQuantity.compareTo(targetQuantity) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "累计出库数量不能超过明细数量");
        }

        decreaseInventory(detail.getInventoryId(), detail.getMaterialId(), quantity);

        detail.setFinishQuantity(newFinishQuantity)
                .setIsFinished(newFinishQuantity.compareTo(targetQuantity) >= 0);
        outputDetailMapper.updateById(detail);

        finishOutputIfAllDetailsFinished(output.getId());
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
                    .setFinishQuantity(BigDecimal.ZERO)
                    .setIsFinished(false);
            outputDetailMapper.insert(entity);
        }
    }

    private void decreaseInventory(Long inventoryId, Long materialId, BigDecimal quantity) {
        InventoryEntity inventory = getRequiredInventory(inventoryId);
        if (!Objects.equals(inventory.getMaterialId(), materialId)) {
            throw new AppException(CommonError.PARAM_INVALID, "来源库存物料与出库物料不一致");
        }
        long now = System.currentTimeMillis();
        int updated = inventoryMapper.decreaseQuantity(inventoryId, quantity, now);
        if (updated == 0) {
            throw new AppException(CommonError.PARAM_INVALID, "库存数量不足");
        }
    }

    private void finishOutputIfAllDetailsFinished(Long outputId) {
        if (outputDetailMapper.countUnfinished(outputId) > 0) {
            return;
        }
        OutputEntity update = new OutputEntity();
        update.setId(outputId);
        update.setStatus(OutputStatus.FINISHED.getValue());
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

    private String resolveBillCode(String billCode) {
        String code = trimToNull(billCode);
        if (code != null) {
            return code;
        }
        return codeRuleService.createCode(CodeRuleField.OUTPUT_BILL_CODE);
    }

    private String resolveUpdateBillCode(String requestBillCode, String existBillCode) {
        String code = trimToNull(requestBillCode);
        return code != null ? code : existBillCode;
    }

}
