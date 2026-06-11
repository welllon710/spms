package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.PurchasePriceEntity;
import com.spms.channel.entity.SupplierEntity;
import com.spms.channel.mapper.PurchasePriceMapper;
import com.spms.channel.model.PurchasePricePageFilter;
import com.spms.channel.service.PurchasePriceService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.spms.common.util.ParamUtils.requireNotNull;

@Service
@RequiredArgsConstructor
public class PurchasePriceServiceImpl extends BaseService<PurchasePriceEntity> implements PurchasePriceService {
    private final PurchasePriceMapper purchasePriceMapper;



    @Override
    public void add(PurchasePriceEntity purchasePriceEntity) {
        requireNotNull(purchasePriceEntity, "请求参数不能为空");
        requireNotNull(purchasePriceEntity.getPrice(), "采购物料不能为空");
        requireNotNull(purchasePriceEntity.getMaterial(), "采购物料不能为空");
        requireNotNull(purchasePriceEntity.getSupplier(), "供应商不能为空");
        initAddEntity(purchasePriceEntity);
        purchasePriceEntity.setMaterialId(purchasePriceEntity.getMaterial().getId());
        purchasePriceEntity.setSupplierId(purchasePriceEntity.getSupplier().getId());
        purchasePriceMapper.insert(purchasePriceEntity);
    }

    @Override
    public PageResult<PurchasePriceEntity> getPage(PageQuery<PurchasePricePageFilter> request) {
        PurchasePricePageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .put("materialId", PurchasePricePageFilter::materialId)
                .put("supplierId", PurchasePricePageFilter::supplierId)
                .putTrim("materialName", PurchasePricePageFilter::materialName)
                .putTrim("materialCode", PurchasePricePageFilter::materialCode)
                .putTrim("supplierName", PurchasePricePageFilter::supplierName)
                .putTrim("supplierCode", PurchasePricePageFilter::supplierCode)
                .put("isDisabled", PurchasePricePageFilter::isDisabled)
                .toMap();
        Page<PurchasePriceEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(purchasePriceMapper.getPageList(page, params), null);
    }

    @Override
    public PurchasePriceEntity getById(String id) {
        return purchasePriceMapper.getById(id);
    }

    @Override
    public void update(PurchasePriceEntity purchasePriceEntity) {
        requireNotNull(purchasePriceEntity, "请求参数不能为空");
        requireNotNull(purchasePriceEntity.getId(), "id不能为空");
        requireNotNull(purchasePriceEntity.getPrice(), "采购物料不能为空");
        requireNotNull(purchasePriceEntity.getMaterial(), "采购物料不能为空");
        requireNotNull(purchasePriceEntity.getSupplier(), "供应商不能为空");
        initUpdateEntity(purchasePriceEntity);
        PurchasePriceEntity entity = PurchasePriceEntity.builder()
                .materialId(purchasePriceEntity.getMaterial().getId())
                .supplierId(purchasePriceEntity.getSupplier().getId())
                .price(purchasePriceEntity.getPrice())
                .build();
        entity.setId(purchasePriceEntity.getId());
        purchasePriceMapper.updateById(entity);
    }

    @Override
    public PurchasePriceEntity getByMaterialAndSupplier(PurchasePriceEntity request) {
        requireNotNull(request, "请求参数不能为空");
        requireNotNull(request.getMaterial(), "采购物料不能为空");
        requireNotNull(request.getSupplier(), "供应商不能为空");
        MaterialEntity material = request.getMaterial();
        SupplierEntity supplier = request.getSupplier();
        return purchasePriceMapper.getByMaterialAndSupplier(material.getId(), supplier.getId());
    }
}

