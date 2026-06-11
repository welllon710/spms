package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchaseDetailEntity;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.mapper.PurchaseDetailMapper;
import com.spms.channel.mapper.PurchaseMapper;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.service.PurchaseService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;

@Service
@AllArgsConstructor
public class PurchaseServiceImpl extends BaseService<PurchaseEntity> implements PurchaseService {

    private final PurchaseMapper purchaseMapper;

    private final PurchaseDetailMapper purchaseDetailMapper;

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
                .setBillCode(request.billCode())
                .setTotalPrice(totalPrice);
        initAddEntity(purchaseEntity);
        purchaseMapper.insert(purchaseEntity);
        Long billId = purchaseEntity.getId();

        ArrayList<PurchaseDetailEntity> list = new ArrayList<>();
        details.forEach(detail -> {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setPrice(detail.getPrice())
                    .setQuantity(detail.getQuantity())
                    .setMaterialId(detail.getMaterialId())
                    .setSupplierId(detail.getSupplierId())
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
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = Wrappers.lambdaQuery(PurchaseDetailEntity.class)
                .eq(PurchaseDetailEntity::getBillId, purchaseEntity.getId());
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailMapper.selectList(wrapper);
        purchaseEntity.setDetailList(purchaseDetailEntities);
        return purchaseEntity;
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
}
