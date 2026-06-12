package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.service.PurchaseService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@AllArgsConstructor
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
    public void update(PurchaseEntity request) {
        requireNotNull(request, "请求参数不能为空");
        List<PurchaseDetailEntity> details = request.getDetails();
        requireNotNull(details, "采购明细不能为空");
        if (details.isEmpty()) {
            throw new AppException(CommonError.PARAM_MISSING, "采购明细不能为空");
        }
        purchaseDetailMapper.delete(Wrappers.<PurchaseDetailEntity>lambdaQuery().eq(PurchaseDetailEntity::getBillId, request.getId()));

        Double totalPrice = calculateTotalPrice(details);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        BeanUtils.copyProperties(request, purchaseEntity);
        purchaseEntity.setReason(request.getReason())
                .setBillCode(request.getBillCode())
                .setTotalPrice(totalPrice);
        initUpdateEntity(purchaseEntity);

        purchaseEntity.setStatus(PurchaseStatus.AUDITING.getValue());
        purchaseMapper.updateById(purchaseEntity);

        ArrayList<PurchaseDetailEntity> list = new ArrayList<>();
        purchaseEntity.getDetails().forEach(detail -> {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setPrice(detail.getPrice())
                    .setQuantity(detail.getQuantity())
                    .setMaterialId(detail.getMaterial().getId())
                    .setSupplierId(detail.getSupplier().getId())
                    .setBillId(purchaseEntity.getId());
            list.add(entity);
        });

        list.forEach(purchaseDetailMapper::insert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFinish(Map<String, Long> request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.get("id"), "采购单id不能为空");
        requireId(request.get("billId"), "采购单号不能为空");
        requireId(request.get("quantity"), "采购数量不能为空");
        PurchaseDetailEntity entity = purchaseDetailMapper.selectById(request.get("id"));

        BigDecimal quantity = BigDecimal.valueOf(request.get("quantity"));
        BigDecimal finishQuantity = BigDecimal.valueOf(entity.getFinishQuantity());
        BigDecimal multiply = quantity.add(finishQuantity);

        LambdaUpdateWrapper<PurchaseDetailEntity> wrapper =
                Wrappers.lambdaUpdate(PurchaseDetailEntity.class)
                        .eq(PurchaseDetailEntity::getId, request.get("id"))
                        .eq(PurchaseDetailEntity::getBillId, request.get("billId"))
                        .set(PurchaseDetailEntity::getFinishQuantity, multiply.doubleValue());
        purchaseDetailMapper.update(null, wrapper);

        List<PurchaseDetailEntity> billEntity = purchaseDetailMapper.selectList(Wrappers.<PurchaseDetailEntity>lambdaQuery()
                .eq(PurchaseDetailEntity::getBillId, request.get("billId")));

        if (billEntity.stream().allMatch(detail -> detail.getQuantity().equals(detail.getFinishQuantity()))) {
            LambdaUpdateWrapper<PurchaseEntity> wrapperEntity = Wrappers.lambdaUpdate(PurchaseEntity.class)
                    .eq(PurchaseEntity::getId, request.get("billId"))
                    .set(PurchaseEntity::getStatus, PurchaseStatus.IN_STORAGE.getValue());
            purchaseMapper.update(null, wrapperEntity);
            billEntity.forEach(detail -> {
                detail.setIsFinished(true);
            });
            purchaseDetailMapper.updateById(billEntity);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(PurchasePageFilter request) {
        requireNotNull(request, "请求参数不能为空");
        List<PurchaseDetailEntity> details = request.details();
        requireNotNull(details, "采购明细不能为空");
        if (details.isEmpty()) {
            throw new AppException(CommonError.PARAM_MISSING, "采购明细不能为空");
        }
        Double totalPrice = calculateTotalPrice(details);

        //新增采购单
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setReason(request.reason())
                .setBillCode(resolveBillCode(request.billCode()))
                .setTotalPrice(totalPrice)
                .setStatus(PurchaseStatus.AUDITING.getValue());
        initAddEntity(purchaseEntity);
        purchaseMapper.insert(purchaseEntity);
        Long billId = purchaseEntity.getId();

        ArrayList<PurchaseDetailEntity> list = new ArrayList<>();
        details.forEach(detail -> {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setPrice(detail.getPrice())
                    .setQuantity(detail.getQuantity())
                    .setMaterialId(detail.getMaterial().getId())
                    .setSupplierId(detail.getSupplier().getId())
                    .setBillId(billId);
            list.add(entity);
        });
        list.forEach(purchaseDetailMapper::insert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseEntity getDetail(Map<String, Object> request) {
        String id = (String) request.get("id");
        if (id == null) {
            throw new AppException(CommonError.PARAM_MISSING, "id不能为空");
        }
        PurchaseEntity purchaseEntity = purchaseMapper.selectById(id);
        List<PurchaseDetailEntity> purchaseDetailList = purchaseDetailMapper.getPurchaseDetailList(purchaseEntity.getId());
        purchaseEntity.setDetails(purchaseDetailList);
        return purchaseEntity;
    }

    @Override
    public void audit(PurchaseEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        PurchaseEntity purchaseEntity = purchaseMapper.selectById(request.getId());
        Integer status = purchaseEntity.getStatus();
        Integer value = PurchaseStatus.AUDITING.getValue();
        if (!status.equals(value)) {
            throw new AppException(CommonError.PARAM_MISSING, "该单据状态无法审核");
        }
        request.setStatus(PurchaseStatus.PURCHASING.getValue());
        purchaseMapper.updateById(request);
    }

    @Override
    public void reject(PurchaseEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        PurchaseEntity purchaseEntity = purchaseMapper.selectById(request.getId());
        Integer status = purchaseEntity.getStatus();
        Integer value = PurchaseStatus.AUDITING.getValue();
        if (!status.equals(value)) {
            throw new AppException(CommonError.PARAM_MISSING, "该单据状态无法驳回");
        }
        request.setStatus(PurchaseStatus.REJECTED.getValue());
        purchaseMapper.updateById(request);
    }


    private LambdaQueryWrapper<PurchaseEntity> buildPageWrapper(Map<String, Object> params) {
        String billCode = (String) params.get("billCode");
        String status = (String) params.get("status");
        return Wrappers.lambdaQuery(PurchaseEntity.class)
                .eq(status != null, PurchaseEntity::getStatus, status)
                .like(billCode != null, PurchaseEntity::getBillCode, billCode)
                .orderByDesc(PurchaseEntity::getId);
    }

    private Double calculateTotalPrice(List<PurchaseDetailEntity> details) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (PurchaseDetailEntity detail : details) {
            requireNotNull(detail, "采购明细不能为空");
            requireNotNull(detail.getPrice(), "采购单价不能为空");
            requireNotNull(detail.getQuantity(), "采购数量不能为空");
            BigDecimal price = BigDecimal.valueOf(detail.getPrice());
            BigDecimal quantity = BigDecimal.valueOf(detail.getQuantity());
            totalPrice = totalPrice.add(price.multiply(quantity));
        }
        return totalPrice.doubleValue();
    }

    private String resolveBillCode(String billCode) {
        String code = trimToNull(billCode);
        if (code != null) {
            return code;
        }
        return codeRuleService.createCode(CodeRuleField.PURCHASE_BILL_CODE);
    }
}
