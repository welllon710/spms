package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.SaleDetailEntity;
import com.spms.channel.entity.SaleEntity;
import com.spms.channel.enums.SaleStatus;
import com.spms.channel.mapper.SaleDetailMapper;
import com.spms.channel.mapper.SaleMapper;
import com.spms.channel.model.SalePageFilter;
import com.spms.channel.service.SaleService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
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
    public void add(SalePageFilter request) {
        requireNotNull(request, "请求参数不能为空");
        List<SaleDetailEntity> details = getDetails(request);
        validateDetails(details);

        SaleEntity sale = new SaleEntity();
        sale.setReason(request.reason())
                .setBillCode(resolveBillCode(request.billCode()))
                .setCustomerId(getCustomerId(request.customerId(), request.customer()))
                .setStatus(SaleStatus.AUDITING.getValue())
                .setTotalPrice(calculateTotalPrice(details));
        initAddEntity(sale);
        saleMapper.insert(sale);

        saveDetails(sale.getId(), details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SaleEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        List<SaleDetailEntity> details = request.getDetails();
        validateDetails(details);

        saleDetailMapper.delete(Wrappers.<SaleDetailEntity>lambdaQuery()
                .eq(SaleDetailEntity::getBillId, request.getId()));

        SaleEntity sale = new SaleEntity();
        BeanUtils.copyProperties(request, sale);
        sale.setCustomerId(getCustomerId(request.getCustomerId(), request.getCustomer()))
                .setStatus(SaleStatus.AUDITING.getValue())
                .setTotalPrice(calculateTotalPrice(details));
        initUpdateEntity(sale);
        saleMapper.updateById(sale);

        saveDetails(sale.getId(), details);
    }

    @Override
    public SaleEntity getDetail(Map<String, Object> request) {
        Long id = getId(request);
        SaleEntity sale = saleMapper.getById(id);
        if (sale == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "销售单不存在");
        }
        sale.setDetails(saleDetailMapper.getSaleDetailList(sale.getId()));
        return sale;
    }

    @Override
    public void audit(SaleEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        SaleEntity sale = saleMapper.selectById(request.getId());
        if (sale == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "销售单不存在");
        }
        Integer status = sale.getStatus();
        if (!SaleStatus.AUDITING.getValue().equals(status)) {
            throw new AppException(CommonError.PARAM_MISSING, "该单据状态无法审核");
        }
        request.setStatus(SaleStatus.OUT_STORAGE.getValue());
        saleMapper.updateById(request);
    }

    @Override
    public void reject(SaleEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireId(request.getId(), "id不能为空");
        SaleEntity sale = saleMapper.selectById(request.getId());
        if (sale == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "销售单不存在");
        }
        Integer status = sale.getStatus();
        if (!SaleStatus.AUDITING.getValue().equals(status)) {
            throw new AppException(CommonError.PARAM_MISSING, "该单据状态无法驳回");
        }
        request.setStatus(SaleStatus.REJECTED.getValue());
        saleMapper.updateById(request);
    }

    private List<SaleDetailEntity> getDetails(SalePageFilter request) {
        if (request.details() != null) {
            return request.details();
        }
        return request.detailList();
    }

    private Long getCustomerId(Long customerId, CustomerEntity customer) {
        Long id = customerId;
        if (id == null && customer != null) {
            id = customer.getId();
        }
        requireId(id, "客户ID不能为空");
        return id;
    }

    private void validateDetails(List<SaleDetailEntity> details) {
        requireNotNull(details, "销售明细不能为空");
        if (details.isEmpty()) {
            throw new AppException(CommonError.PARAM_MISSING, "销售明细不能为空");
        }
        for (SaleDetailEntity detail : details) {
            requireNotNull(detail, "销售明细不能为空");
            requireNotNull(detail.getPrice(), "销售单价不能为空");
            requireNotNull(detail.getQuantity(), "销售数量不能为空");
            if (detail.getMaterialId() == null && detail.getMaterial() == null) {
                throw new AppException(CommonError.PARAM_MISSING, "销售物料不能为空");
            }
        }
    }

    private void saveDetails(Long billId, List<SaleDetailEntity> details) {
        ArrayList<SaleDetailEntity> list = new ArrayList<>();
        details.forEach(detail -> {
            SaleDetailEntity entity = new SaleDetailEntity();
            entity.setPrice(detail.getPrice())
                    .setQuantity(detail.getQuantity())
                    .setMaterialId(getMaterialId(detail))
                    .setBillId(billId);
            list.add(entity);
        });
        list.forEach(saleDetailMapper::insert);
    }

    private Long getMaterialId(SaleDetailEntity detail) {
        Long materialId = detail.getMaterialId();
        if (materialId == null && detail.getMaterial() != null) {
            materialId = detail.getMaterial().getId();
        }
        requireId(materialId, "物料ID不能为空");
        return materialId;
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

    private Double calculateTotalPrice(List<SaleDetailEntity> details) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (SaleDetailEntity detail : details) {
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
        return codeRuleService.createCode(CodeRuleField.SALE_BILL_CODE);
    }
}
