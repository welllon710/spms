package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.IdRequest;
import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.SaleDetailEntity;
import com.spms.channel.entity.SaleEntity;
import com.spms.channel.enums.SaleStatus;
import com.spms.channel.mapper.SaleDetailMapper;
import com.spms.channel.mapper.SaleMapper;
import com.spms.channel.model.SaleAddRequest;
import com.spms.channel.model.SaleUpdateRequest;
import com.spms.channel.model.SalePageFilter;
import com.spms.channel.service.SaleService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
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
public class SaleServiceImpl extends BaseService<SaleEntity> implements SaleService {
    private final SaleMapper saleMapper;
    private final SaleDetailMapper saleDetailMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<SaleEntity> getPage(PageQuery<SalePageFilter> request) {
        SalePageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("billCode", SalePageFilter::billCode)
                .putTrim("status", SalePageFilter::status)
                .put("customerId", SalePageFilter::customerId)
                .putTrim("customerName", SalePageFilter::customerName)
                .putTrim("customerCode", SalePageFilter::customerCode)
                .toMap();
        Page<SaleEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(saleMapper.getPageList(page, params), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SaleAddRequest request) {
        List<SaleDetailEntity> details = request.details();

        SaleEntity sale = new SaleEntity();
        sale.setReason(request.reason())
                .setBillCode(resolveBillCode(request.billCode()))
                .setCustomerId(getCustomerId(request.customerId(), request.customer()))
                .setStatus(SaleStatus.AUDITING.getValue())
                .setTotalPrice(calculateTotalPrice(details));
        saleMapper.insert(sale);

        saveDetails(sale.getId(), details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SaleUpdateRequest request) {
        List<SaleDetailEntity> details = request.details();
        SaleEntity exist = getRequiredSale(request.id());
        if (!SaleStatus.AUDITING.getValue().equals(exist.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "当前销售单状态无法修改");
        }

        saleDetailMapper.delete(Wrappers.<SaleDetailEntity>lambdaQuery()
                .eq(SaleDetailEntity::getBillId, request.id()));

        SaleEntity sale = new SaleEntity();
        sale.setId(request.id());
        sale.setBillCode(resolveUpdateBillCode(request.billCode(), exist.getBillCode()))
                .setReason(request.reason())
                .setCustomerId(getCustomerId(request.customerId(), request.customer()))
                .setStatus(SaleStatus.AUDITING.getValue())
                .setTotalPrice(calculateTotalPrice(details));
        saleMapper.updateById(sale);

        saveDetails(sale.getId(), details);
    }

    @Override
    public SaleEntity getDetail(IdRequest request) {
        SaleEntity sale = getRequiredSale(request.id());
        sale.setDetails(saleDetailMapper.getSaleDetailList(sale.getId()));
        return sale;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(IdRequest request) {
        SaleEntity sale = getRequiredSale(request.id());
        if (!SaleStatus.AUDITING.getValue().equals(sale.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法审核");
        }
        SaleEntity update = new SaleEntity();
        update.setId(request.id());
        update.setStatus(SaleStatus.OUT_STORAGE.getValue());
        saleMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(RejectRequest request) {
        SaleEntity sale = getRequiredSale(request.id());
        if (!SaleStatus.AUDITING.getValue().equals(sale.getStatus())) {
            throw new AppException(CommonError.PARAM_INVALID, "该单据状态无法驳回");
        }
        SaleEntity update = new SaleEntity();
        update.setId(request.id());
        update.setStatus(SaleStatus.REJECTED.getValue());
        update.setRejectReason(request.rejectReason());
        saleMapper.updateById(update);
    }

    private Long getCustomerId(Long customerId, CustomerEntity customer) {
        Long id = customerId;
        if (id == null && customer != null) {
            id = customer.getId();
        }
        requireId(id, "客户ID不能为空");
        return id;
    }

    private void saveDetails(Long billId, List<SaleDetailEntity> details) {
        for (SaleDetailEntity detail : details) {
            SaleDetailEntity entity = new SaleDetailEntity();
            entity.setPrice(detail.getPrice())
                    .setQuantity(detail.getQuantity())
                    .setMaterialId(getMaterialId(detail))
                    .setBillId(billId);
            saleDetailMapper.insert(entity);
        }
    }

    private Long getMaterialId(SaleDetailEntity detail) {
        Long materialId = detail.getMaterialId();
        if (materialId == null && detail.getMaterial() != null) {
            materialId = detail.getMaterial().getId();
        }
        requireId(materialId, "物料ID不能为空");
        return materialId;
    }


    private BigDecimal calculateTotalPrice(List<SaleDetailEntity> details) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (SaleDetailEntity detail : details) {
            if (detail.getPrice() == null || detail.getQuantity() == null) {
                throw new AppException(CommonError.PARAM_MISSING, "明细单价和数量不能为空");
            }
            totalPrice = totalPrice.add(detail.getPrice().multiply(detail.getQuantity()));
        }
        return totalPrice;
    }

    private SaleEntity getRequiredSale(Long id) {
        requireId(id, "销售单ID不能为空");
        SaleEntity sale = saleMapper.getById(id);
        if (sale == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "销售单不存在");
        }
        return sale;
    }

    private String resolveBillCode(String billCode) {
        String code = trimToNull(billCode);
        if (code != null) {
            return code;
        }
        return codeRuleService.createCode(CodeRuleField.SALE_BILL_CODE);
    }

    private String resolveUpdateBillCode(String requestBillCode, String existBillCode) {
        String code = trimToNull(requestBillCode);
        return code != null ? code : existBillCode;
    }
}
