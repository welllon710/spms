package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchaseDetailEntity;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.enums.PurchaseStatus;
import com.spms.channel.mapper.PurchaseDetailMapper;
import com.spms.channel.mapper.PurchaseMapper;
import com.spms.channel.mapper.PurchasePriceMapper;
import com.spms.base.IdRequest;
import com.spms.base.RejectRequest;
import com.spms.channel.model.PurchaseAddRequest;
import com.spms.channel.model.PurchaseFinishRequest;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.model.PurchaseUpdateRequest;
import com.spms.channel.service.PurchaseService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requirePositiveQuantity;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl extends BaseService<PurchaseEntity> implements PurchaseService {

    private final PurchaseMapper purchaseMapper;

    private final PurchaseDetailMapper purchaseDetailMapper;

    private final PurchasePriceMapper purchasePriceMapper;

    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<PurchaseEntity> getPage(PageQuery<PurchasePageFilter> request) {
        PurchasePageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("billCode", PurchasePageFilter::billCode)
                .putTrim("status", PurchasePageFilter::status)
                .toMap();
        Page<PurchaseEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(purchaseMapper.selectPage(page, buildPageWrapper(params)), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PurchaseUpdateRequest request) {
        List<PurchaseDetailEntity> details = request.details();
        PurchaseEntity exist = getRequiredPurchase(request.id());
        if (!PurchaseStatus.AUDITING.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前采购单状态无法修改");
        }
        purchaseDetailMapper.delete(Wrappers.<PurchaseDetailEntity>lambdaQuery().eq(PurchaseDetailEntity::getBillId, request.id()));

        BigDecimal totalPrice = calculateTotalPrice(details);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(request.id());
        purchaseEntity.setReason(request.reason())
                .setBillCode(resolveUpdateBillCode(request.billCode(), exist.getBillCode()))
                .setTotalPrice(totalPrice)
                .setStatus(PurchaseStatus.AUDITING.getValue());
        purchaseMapper.updateById(purchaseEntity);

        saveDetails(purchaseEntity.getId(), details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFinish(PurchaseFinishRequest request) {
        BigDecimal quantity = request.quantity();

        PurchaseDetailEntity detail = purchaseDetailMapper.selectById(request.id());
        if (detail == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "采购明细不存在");
        }
        if (!detail.getBillId().equals(request.billId())) {
            throw new AppException(CommonError.PARAM_INVALID, "采购明细与采购单不匹配");
        }

        BigDecimal targetQuantity = requirePositiveQuantity(detail.getQuantity(), "采购明细数量配置不正确");
        BigDecimal oldFinishQuantity = detail.getFinishQuantity() == null ? BigDecimal.ZERO : detail.getFinishQuantity();
        BigDecimal newFinishQuantity = oldFinishQuantity.add(quantity);
        if (newFinishQuantity.compareTo(targetQuantity) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "累计采购数量不能超过明细数量");
        }

        detail.setFinishQuantity(newFinishQuantity)
                .setIsFinished(newFinishQuantity.compareTo(targetQuantity) >= 0);
        purchaseDetailMapper.updateById(detail);

        finishPurchaseIfAllDetailsFinished(request.billId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(PurchaseAddRequest request) {
        List<PurchaseDetailEntity> details = request.details();
        BigDecimal totalPrice = calculateTotalPrice(details);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setReason(request.reason())
                .setBillCode(resolveBillCode(request.billCode()))
                .setTotalPrice(totalPrice)
                .setStatus(PurchaseStatus.AUDITING.getValue());
        purchaseMapper.insert(purchaseEntity);

        saveDetails(purchaseEntity.getId(), details);
    }

    private void saveDetails(Long billId, List<PurchaseDetailEntity> details) {
        for (PurchaseDetailEntity detail : details) {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setPrice(detail.getPrice())
                    .setQuantity(detail.getQuantity())
                    .setMaterialId(getMaterialId(detail))
                    .setSupplierId(getSupplierId(detail))
                    .setBillId(billId);
            purchaseDetailMapper.insert(entity);
        }
    }

    @Override
    public PurchaseEntity getDetail(IdRequest request) {
        PurchaseEntity purchase = getRequiredPurchase(request.id());
        purchase.setDetails(purchaseDetailMapper.getPurchaseDetailList(purchase.getId()));
        return purchase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(IdRequest request) {
        PurchaseEntity exist = getRequiredPurchase(request.id());
        if (!PurchaseStatus.AUDITING.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
        }
        PurchaseEntity update = new PurchaseEntity();
        update.setId(request.id());
        update.setStatus(PurchaseStatus.PURCHASING.getValue());
        purchaseMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(RejectRequest request) {
        PurchaseEntity exist = getRequiredPurchase(request.id());
        if (!PurchaseStatus.AUDITING.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法驳回");
        }
        PurchaseEntity update = new PurchaseEntity();
        update.setId(request.id());
        update.setStatus(PurchaseStatus.REJECTED.getValue());
        update.setRejectReason(request.rejectReason());
        purchaseMapper.updateById(update);
    }


    private PurchaseEntity getRequiredPurchase(Long id) {
        requireId(id, "采购单ID不能为空");
        PurchaseEntity purchase = purchaseMapper.selectById(id);
        if (purchase == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "采购单不存在");
        }
        return purchase;
    }

    private void finishPurchaseIfAllDetailsFinished(Long billId) {
        if (purchaseDetailMapper.countUnfinished(billId) > 0) {
            return;
        }
        PurchaseEntity update = new PurchaseEntity();
        update.setId(billId);
        update.setStatus(PurchaseStatus.IN_STORAGE.getValue());
        purchaseMapper.updateById(update);
    }

    private LambdaQueryWrapper<PurchaseEntity> buildPageWrapper(Map<String, Object> params) {
        String billCode = (String) params.get("billCode");
        String status = (String) params.get("status");
        return Wrappers.lambdaQuery(PurchaseEntity.class)
                .eq(status != null, PurchaseEntity::getStatus, status)
                .like(billCode != null, PurchaseEntity::getBillCode, billCode)
                .orderByDesc(PurchaseEntity::getId);
    }

    private BigDecimal calculateTotalPrice(List<PurchaseDetailEntity> details) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (PurchaseDetailEntity detail : details) {
            if (detail.getPrice() == null || detail.getQuantity() == null) {
                throw new AppException(CommonError.PARAM_MISSING, "明细单价和数量不能为空");
            }
            totalPrice = totalPrice.add(detail.getPrice().multiply(detail.getQuantity()));
        }
        return totalPrice;
    }

    private String resolveBillCode(String billCode) {
        String code = trimToNull(billCode);
        if (code != null) {
            return code;
        }
        return codeRuleService.createCode(CodeRuleField.PURCHASE_BILL_CODE);
    }

    private String resolveUpdateBillCode(String requestBillCode, String existBillCode) {
        String code = trimToNull(requestBillCode);
        return code != null ? code : existBillCode;
    }

    private Long getMaterialId(PurchaseDetailEntity detail) {
        if (detail.getMaterialId() != null) {
            return detail.getMaterialId();
        }
        if (detail.getMaterial() != null) {
            return detail.getMaterial().getId();
        }
        return null;
    }

    private Long getSupplierId(PurchaseDetailEntity detail) {
        if (detail.getSupplierId() != null) {
            return detail.getSupplierId();
        }
        if (detail.getSupplier() != null) {
            return detail.getSupplier().getId();
        }
        return null;
    }
}
