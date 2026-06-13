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
import com.spms.wms.entity.InputDetailEntity;
import com.spms.wms.entity.InputEntity;
import com.spms.wms.entity.InventoryEntity;
import com.spms.wms.entity.StorageEntity;
import com.spms.wms.enums.InputStatus;
import com.spms.wms.enums.InputType;
import com.spms.wms.mapper.InputDetailMapper;
import com.spms.wms.mapper.InputMapper;
import com.spms.wms.mapper.InventoryMapper;
import com.spms.wms.model.InputFinishRequest;
import com.spms.wms.model.InputPageFilter;
import com.spms.wms.service.InputService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class InputServiceImpl extends BaseService<InputEntity> implements InputService {
    private static final int INVENTORY_TYPE_STORAGE = 1;

    private final InputMapper inputMapper;
    private final InputDetailMapper inputDetailMapper;
    private final InventoryMapper inventoryMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<InputEntity> getPage(PageQuery<InputPageFilter> request) {
        InputPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("billCode", InputPageFilter::billCode)
                .put("status", InputPageFilter::status)
                .put("type", InputPageFilter::type)
                .put("purchaseId", InputPageFilter::purchaseId)
                .put("moveId", InputPageFilter::moveId)
                .toMap();
        Page<InputEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(inputMapper.getPageList(page, params), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(InputEntity request) {
        validateInput(request, false);
        List<InputDetailEntity> details = request.getDetails();

        InputEntity input = new InputEntity();
        BeanUtils.copyProperties(request, input);
        input.setBillCode(resolveBillCode(request.getBillCode()))
                .setStatus(InputStatus.AUDITING.getValue())
                .setType(request.getType() == null ? InputType.NORMAL.getValue() : request.getType());
        initAddEntity(input);
        inputMapper.insert(input);

        saveDetails(input.getId(), details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(InputEntity request) {
        validateInput(request, true);
        InputEntity exist = getRequiredInput(request.getId());
        if (InputStatus.INPUTTING.getValue().equals(exist.getStatus())
                || InputStatus.FINISHED.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前入库单状态无法修改");
        }

        inputDetailMapper.delete(Wrappers.<InputDetailEntity>lambdaQuery()
                .eq(InputDetailEntity::getBillId, request.getId()));

        InputEntity input = new InputEntity();
        BeanUtils.copyProperties(request, input);
        input.setBillCode(resolveUpdateBillCode(request.getBillCode(), exist.getBillCode()))
                .setStatus(InputStatus.AUDITING.getValue())
                .setType(request.getType() == null ? InputType.NORMAL.getValue() : request.getType());
        initUpdateEntity(input);
        inputMapper.updateById(input);

        saveDetails(input.getId(), request.getDetails());
    }

    @Override
    public InputEntity getDetail(Map<String, Object> request) {
        Long id = getId(request);
        InputEntity input = getRequiredInput(id);
        input.setDetails(inputDetailMapper.getByBillId(input.getId()));
        return input;
    }

    @Override
    public void audit(InputEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        InputEntity input = getRequiredInput(request.getId());
        if (!InputStatus.AUDITING.getValue().equals(input.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
        }
        InputEntity update = new InputEntity();
        update.setId(request.getId());
        update.setStatus(InputStatus.INPUTTING.getValue());
        initUpdateEntity(update);
        inputMapper.updateById(update);
    }

    @Override
    public void reject(InputEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        requireText(request.getRejectReason(), "驳回原因不能为空");
        InputEntity input = getRequiredInput(request.getId());
        if (!InputStatus.AUDITING.getValue().equals(input.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法驳回");
        }
        InputEntity update = new InputEntity();
        update.setId(request.getId());
        update.setStatus(InputStatus.REJECTED.getValue());
        update.setRejectReason(request.getRejectReason());
        initUpdateEntity(update);
        inputMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFinish(InputFinishRequest request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.id(), "入库明细ID不能为空");
        Long storageId = getStorageId(request);
        BigDecimal quantity = requirePositiveQuantity(request.quantity(), "本次入库数量必须大于0");

        InputDetailEntity detail = inputDetailMapper.selectById(request.id());
        if (detail == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "入库明细不存在");
        }
        InputEntity input = getRequiredInput(detail.getBillId());
        if (!InputStatus.INPUTTING.getValue().equals(input.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前入库单状态无法执行入库");
        }

        BigDecimal targetQuantity = requirePositiveQuantity(detail.getQuantity(), "入库明细数量配置不正确");
        BigDecimal oldFinishQuantity = BigDecimal.valueOf(detail.getFinishQuantity() == null ? 0D : detail.getFinishQuantity());
        BigDecimal newFinishQuantity = oldFinishQuantity.add(quantity);
        if (newFinishQuantity.compareTo(targetQuantity) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "累计入库数量不能超过明细数量");
        }

        detail.setFinishQuantity(newFinishQuantity.doubleValue())
                .setIsFinished(newFinishQuantity.compareTo(targetQuantity) >= 0);
        initUpdateBaseEntity(detail);
        inputDetailMapper.updateById(detail);

        increaseInventory(detail.getMaterialId(), storageId, quantity);
        finishInputIfAllDetailsFinished(input.getId());
    }

    private void validateInput(InputEntity input, boolean requireId) {
        requireNotNull(input, "请求参数不能为空");
        if (requireId) {
            requireId(input.getId(), "入库单ID不能为空");
        }
        List<InputDetailEntity> details = input.getDetails();
        requireNotNull(details, "入库明细不能为空");
        if (details.isEmpty()) {
            throw new AppException(CommonError.PARAM_MISSING, "入库明细不能为空");
        }
        for (InputDetailEntity detail : details) {
            requireNotNull(detail, "入库明细不能为空");
            requireId(getMaterialId(detail), "入库物料不能为空");
            requirePositiveQuantity(detail.getQuantity(), "入库数量必须大于0");
        }
    }

    private void saveDetails(Long billId, List<InputDetailEntity> details) {
        for (InputDetailEntity detail : details) {
            InputDetailEntity entity = new InputDetailEntity();
            entity.setBillId(billId)
                    .setMaterialId(getMaterialId(detail))
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(0D)
                    .setIsFinished(false);
            initAddBaseEntity(entity);
            inputDetailMapper.insert(entity);
        }
    }

    private Long getMaterialId(InputDetailEntity detail) {
        if (detail.getMaterialId() != null) {
            return detail.getMaterialId();
        }
        if (detail.getMaterial() != null) {
            return detail.getMaterial().getId();
        }
        return null;
    }

    private Long getStorageId(InputFinishRequest request) {
        Long storageId = request.storageId();
        StorageEntity storage = request.storage();
        if (storageId == null && storage != null) {
            storageId = storage.getId();
        }
        requireId(storageId, "入库仓库不能为空");
        return storageId;
    }

    private void increaseInventory(Long materialId, Long storageId, BigDecimal quantity) {
        requireId(materialId, "入库物料不能为空");
        InventoryEntity inventory = inventoryMapper.selectOne(Wrappers.<InventoryEntity>lambdaQuery()
                .eq(InventoryEntity::getMaterialId, materialId)
                .eq(InventoryEntity::getStorageId, storageId)
                .eq(InventoryEntity::getType, INVENTORY_TYPE_STORAGE)
                .last("limit 1"));
        if (inventory == null) {
            inventory = new InventoryEntity();
            inventory.setMaterialId(materialId)
                    .setStorageId(storageId)
                    .setType(INVENTORY_TYPE_STORAGE)
                    .setQuantity(quantity.doubleValue());
            initAddBaseEntity(inventory);
            inventoryMapper.insert(inventory);
            return;
        }
        BigDecimal oldQuantity = BigDecimal.valueOf(inventory.getQuantity() == null ? 0D : inventory.getQuantity());
        inventory.setQuantity(oldQuantity.add(quantity).doubleValue());
        initUpdateBaseEntity(inventory);
        inventoryMapper.updateById(inventory);
    }

    private void finishInputIfAllDetailsFinished(Long inputId) {
        List<InputDetailEntity> details = inputDetailMapper.selectList(Wrappers.<InputDetailEntity>lambdaQuery()
                .eq(InputDetailEntity::getBillId, inputId));
        boolean allFinished = !details.isEmpty()
                && details.stream().allMatch(detail -> Boolean.TRUE.equals(detail.getIsFinished()));
        if (!allFinished) {
            return;
        }
        InputEntity update = new InputEntity();
        update.setId(inputId);
        update.setStatus(InputStatus.FINISHED.getValue());
        initUpdateEntity(update);
        inputMapper.updateById(update);
    }

    private InputEntity getRequiredInput(Long id) {
        requireId(id, "入库单ID不能为空");
        InputEntity input = inputMapper.getById(id);
        if (input == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "入库单不存在");
        }
        return input;
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
        return codeRuleService.createCode(CodeRuleField.INPUT_BILL_CODE);
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
