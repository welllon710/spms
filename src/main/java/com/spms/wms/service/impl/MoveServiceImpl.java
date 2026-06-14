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
import com.spms.wms.entity.MoveDetailEntity;
import com.spms.wms.entity.MoveEntity;
import com.spms.wms.enums.MoveStatus;
import com.spms.wms.mapper.InventoryMapper;
import com.spms.wms.mapper.MoveDetailMapper;
import com.spms.wms.mapper.MoveMapper;
import com.spms.wms.model.MoveAddRequest;
import com.spms.wms.model.MoveFinishRequest;
import com.spms.wms.model.MoveUpdateRequest;
import com.spms.wms.model.MovePageFilter;
import com.spms.wms.service.MoveService;
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
public class MoveServiceImpl extends BaseService<MoveEntity> implements MoveService {
    private final MoveMapper moveMapper;
    private final MoveDetailMapper moveDetailMapper;
    private final InventoryMapper inventoryMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<MoveEntity> getPage(PageQuery<MovePageFilter> request) {
        MovePageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("billCode", MovePageFilter::billCode)
                .put("status", MovePageFilter::status)
                .put("storageId", MovePageFilter::storageId)
                .toMap();
        Page<MoveEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(moveMapper.getPageList(page, params), null);
    }

    @Override
    public MoveEntity getDetail(IdRequest request) {
        MoveEntity move = getRequiredMove(request.id());
        move.setDetails(moveDetailMapper.getByBillId(move.getId()));
        return move;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MoveAddRequest request) {
        MoveEntity move = new MoveEntity();
        move.setBillCode(resolveBillCode(request.billCode()))
                .setStatus(MoveStatus.AUDITING.getValue())
                .setStorageId(request.storageId());
        moveMapper.insert(move);

        saveDetails(move.getId(), request.details());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MoveUpdateRequest request) {
        MoveEntity exist = getRequiredMove(request.id());
        if (MoveStatus.MOVING.getValue().equals(exist.getStatus())
                || MoveStatus.FINISHED.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前移库单状态无法修改");
        }

        moveDetailMapper.delete(Wrappers.<MoveDetailEntity>lambdaQuery()
                .eq(MoveDetailEntity::getBillId, request.id()));

        MoveEntity move = new MoveEntity();
        move.setId(request.id());
        move.setBillCode(resolveUpdateBillCode(request.billCode(), exist.getBillCode()))
                .setStatus(MoveStatus.AUDITING.getValue())
                .setStorageId(request.storageId());
        moveMapper.updateById(move);

        saveDetails(move.getId(), request.details());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(IdRequest request) {
        MoveEntity move = getRequiredMove(request.id());
        if (!MoveStatus.AUDITING.getValue().equals(move.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
        }
        MoveEntity update = new MoveEntity();
        update.setId(request.id());
        update.setStatus(MoveStatus.MOVING.getValue());
        moveMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(RejectRequest request) {
        MoveEntity move = getRequiredMove(request.id());
        if (!MoveStatus.AUDITING.getValue().equals(move.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法驳回");
        }
        MoveEntity update = new MoveEntity();
        update.setId(request.id());
        update.setStatus(MoveStatus.REJECTED.getValue());
        update.setRejectReason(request.rejectReason());
        moveMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFinish(MoveFinishRequest request) {
        BigDecimal quantity = request.quantity();

        MoveDetailEntity detail = moveDetailMapper.selectById(request.id());
        if (detail == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "移库明细不存在");
        }
        MoveEntity move = getRequiredMove(detail.getBillId());
        if (!MoveStatus.MOVING.getValue().equals(move.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前移库单状态无法执行移库");
        }

        BigDecimal targetQuantity = requirePositiveQuantity(detail.getQuantity(), "移库明细数量配置不正确");
        BigDecimal oldFinishQuantity = detail.getFinishQuantity() == null ? BigDecimal.ZERO : detail.getFinishQuantity();
        BigDecimal newFinishQuantity = oldFinishQuantity.add(quantity);
        if (newFinishQuantity.compareTo(targetQuantity) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "累计移库数量不能超过明细数量");
        }

        moveInventory(detail.getInventoryId(), move.getStorageId(), quantity);

        detail.setFinishQuantity(newFinishQuantity)
                .setIsFinished(newFinishQuantity.compareTo(targetQuantity) >= 0);
        moveDetailMapper.updateById(detail);

        finishMoveIfAllDetailsFinished(move.getId());
    }

    private void saveDetails(Long billId, List<MoveDetailEntity> details) {
        for (MoveDetailEntity detail : details) {
            Long inventoryId = getInventoryId(detail);
            MoveDetailEntity entity = new MoveDetailEntity();
            entity.setBillId(billId)
                    .setInventoryId(inventoryId)
                    .setQuantity(detail.getQuantity())
                    .setFinishQuantity(BigDecimal.ZERO)
                    .setIsFinished(false);
            moveDetailMapper.insert(entity);
        }
    }

    private void moveInventory(Long sourceInventoryId, Long targetStorageId, BigDecimal quantity) {
        InventoryEntity source = getRequiredInventory(sourceInventoryId);
        long now = System.currentTimeMillis();
        int decreased = inventoryMapper.decreaseForMove(sourceInventoryId, quantity, now);
        if (decreased == 0) {
            throw new AppException(CommonError.PARAM_INVALID, "库存数量不足");
        }
        // 原子增加目标库存；记录不存在时新建。
        // 安全前提：(material_id, storage_id, type) 上存在唯一索引以防并发重复插入。
        int increased = inventoryMapper.increaseForMove(
                source.getMaterialId(), targetStorageId, source.getType(), quantity, now);
        if (increased == 0) {
            InventoryEntity newInventory = new InventoryEntity();
            newInventory.setMaterialId(source.getMaterialId())
                    .setQuantity(quantity)
                    .setType(source.getType())
                    .setStorageId(targetStorageId)
                    .setStructureId(source.getStructureId());
            inventoryMapper.insert(newInventory);
        }
    }

    private void finishMoveIfAllDetailsFinished(Long moveId) {
        if (moveDetailMapper.countUnfinished(moveId) > 0) {
            return;
        }
        MoveEntity update = new MoveEntity();
        update.setId(moveId);
        update.setStatus(MoveStatus.FINISHED.getValue());
        moveMapper.updateById(update);
    }

    private MoveEntity getRequiredMove(Long id) {
        requireId(id, "移库单ID不能为空");
        MoveEntity move = moveMapper.getById(id);
        if (move == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "移库单不存在");
        }
        return move;
    }

    private InventoryEntity getRequiredInventory(Long id) {
        requireId(id, "来源库存不能为空");
        InventoryEntity inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "来源库存不存在");
        }
        return inventory;
    }

    private Long getInventoryId(MoveDetailEntity detail) {
        if (detail.getInventoryId() != null) {
            return detail.getInventoryId();
        }
        if (detail.getInventory() != null) {
            return detail.getInventory().getId();
        }
        return null;
    }


    private String resolveBillCode(String billCode) {
        String code = trimToNull(billCode);
        if (code != null) {
            return code;
        }
        return codeRuleService.createCode(CodeRuleField.MOVE_BILL_CODE);
    }

    private String resolveUpdateBillCode(String requestBillCode, String existBillCode) {
        String code = trimToNull(requestBillCode);
        return code != null ? code : existBillCode;
    }

}
