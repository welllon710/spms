package com.spms.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.IdRequest;
import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.enums.PurchaseStatus;
import com.spms.channel.mapper.PurchaseMapper;
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
import com.spms.wms.model.InputAddRequest;
import com.spms.wms.model.InputFinishRequest;
import com.spms.wms.model.InputPageFilter;
import com.spms.wms.model.InputUpdateRequest;
import com.spms.wms.service.InputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spms.base.RejectRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requirePositiveQuantity;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class InputServiceImpl extends BaseService<InputEntity> implements InputService {
    private static final int INVENTORY_TYPE_STORAGE = 1;

    private final InputMapper inputMapper;
    private final InputDetailMapper inputDetailMapper;
    private final InventoryMapper inventoryMapper;
    private final CodeRuleService codeRuleService;
    private final PurchaseMapper purchaseMapper;

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
    public void add(InputAddRequest request) {
        InputEntity input = new InputEntity();
        input.setBillCode(resolveBillCode(request.billCode()))
                .setStatus(InputStatus.AUDITING.getValue())
                .setType(request.type() == null ? InputType.NORMAL.getValue() : request.type())
                .setMoveId(request.moveId())
                .setOrderId(request.orderId())
                .setPurchaseId(request.purchaseId())
                .setStructureId(request.structureId());
        inputMapper.insert(input);

        saveDetails(input.getId(), request.details());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(InputUpdateRequest request) {
        InputEntity exist = getRequiredInput(request.id());
        if (InputStatus.INPUTTING.getValue().equals(exist.getStatus())
                || InputStatus.FINISHED.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前入库单状态无法修改");
        }

        inputDetailMapper.delete(Wrappers.<InputDetailEntity>lambdaQuery()
                .eq(InputDetailEntity::getBillId, request.id()));

        InputEntity input = new InputEntity();
        input.setId(request.id());
        input.setBillCode(resolveUpdateBillCode(request.billCode(), exist.getBillCode()))
                .setStatus(InputStatus.AUDITING.getValue())
                .setType(request.type() == null ? InputType.NORMAL.getValue() : request.type())
                .setMoveId(request.moveId())
                .setOrderId(request.orderId())
                .setPurchaseId(request.purchaseId())
                .setStructureId(request.structureId());
        inputMapper.updateById(input);

        saveDetails(input.getId(), request.details());
    }

    @Override
    public InputEntity getDetail(IdRequest request) {
        InputEntity input = getRequiredInput(request.id());
        input.setDetails(inputDetailMapper.getByBillId(input.getId()));
        return input;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(IdRequest request) {
        InputEntity input = getRequiredInput(request.id());
        if (!InputStatus.AUDITING.getValue().equals(input.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
        }
        InputEntity update = new InputEntity();
        update.setId(request.id());
        update.setStatus(InputStatus.INPUTTING.getValue());
        inputMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(RejectRequest request) {
        InputEntity input = getRequiredInput(request.id());
        if (!InputStatus.AUDITING.getValue().equals(input.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法驳回");
        }
        InputEntity update = new InputEntity();
        update.setId(request.id());
        update.setStatus(InputStatus.REJECTED.getValue());
        update.setRejectReason(request.rejectReason());
        inputMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFinish(InputFinishRequest request) {
        Long storageId = getStorageId(request);
        BigDecimal quantity = request.quantity();

        InputDetailEntity detail = inputDetailMapper.selectById(request.id());
        if (detail == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "入库明细不存在");
        }
        InputEntity input = getRequiredInput(detail.getBillId());
        if (!InputStatus.INPUTTING.getValue().equals(input.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前入库单状态无法执行入库");
        }

        BigDecimal targetQuantity = requirePositiveQuantity(detail.getQuantity(), "入库明细数量配置不正确");
        BigDecimal oldFinishQuantity = detail.getFinishQuantity() == null ? BigDecimal.ZERO : detail.getFinishQuantity();
        BigDecimal newFinishQuantity = oldFinishQuantity.add(quantity);
        if (newFinishQuantity.compareTo(targetQuantity) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "累计入库数量不能超过明细数量");
        }

        detail.setFinishQuantity(newFinishQuantity)
                .setIsFinished(newFinishQuantity.compareTo(targetQuantity) >= 0);
        inputDetailMapper.updateById(detail);

        increaseInventory(detail.getMaterialId(), storageId, quantity);
        finishInputIfAllDetailsFinished(input.getId());
    }

    private void saveDetails(Long billId, List<InputDetailEntity> details) {
        for (InputDetailEntity detail : details) {
            InputDetailEntity entity = new InputDetailEntity();
            entity.setBillId(billId)
                    .setMaterialId(getMaterialId(detail))
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(BigDecimal.ZERO)
                    .setIsFinished(false);
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
        long now = System.currentTimeMillis();
        int updated = inventoryMapper.increaseQuantity(materialId, storageId, INVENTORY_TYPE_STORAGE, quantity, now);
        if (updated > 0) {
            return;
        }
        // 记录不存在时新建。要求 (material_id, storage_id, type) 上有唯一索引以防并发重复插入。
        InventoryEntity inventory = new InventoryEntity();
        inventory.setMaterialId(materialId)
                .setStorageId(storageId)
                .setType(INVENTORY_TYPE_STORAGE)
                .setQuantity(quantity);
        inventoryMapper.insert(inventory);
    }

    private void finishInputIfAllDetailsFinished(Long inputId) {
        if (inputDetailMapper.countUnfinished(inputId) > 0) {
            return;
        }
        InputEntity update = new InputEntity();
        update.setId(inputId);
        update.setStatus(InputStatus.FINISHED.getValue());
        inputMapper.updateById(update);

        InputEntity input = getRequiredInput(inputId);

        finishPurchaseIfPurchaseInput(input);
    }

    private void finishPurchaseIfPurchaseInput(InputEntity input) {
        if (!InputType.PURCHASE.getValue().equals(input.getType()) || input.getPurchaseId() == null) {
            return;
        }
        PurchaseEntity update = new PurchaseEntity();
        update.setId(input.getPurchaseId());
        update.setStatus(PurchaseStatus.FINISHED.getValue());
        purchaseMapper.updateById(update);
    }

    private InputEntity getRequiredInput(Long id) {
        requireId(id, "入库单ID不能为空");
        InputEntity input = inputMapper.getById(id);
        if (input == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "入库单不存在");
        }
        return input;
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
        return code != null ? code : existBillCode;
    }

}
