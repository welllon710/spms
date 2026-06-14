package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchasePriceEntity;
import com.spms.channel.entity.SalePriceEntity;
import com.spms.channel.mapper.SalePriceMapper;
import com.spms.channel.model.PurchasePricePageFilter;
import com.spms.channel.service.SalePriceService;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.spms.common.util.ParamUtils.requireNotNull;

@Service
@AllArgsConstructor
public class SalePriceServiceImpl extends BaseService<SalePriceEntity> implements SalePriceService {

    private final SalePriceMapper salePriceMapper;

    @Override
    public PageResult<SalePriceEntity> getPage(PageQuery request) {

        Page<SalePriceEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(salePriceMapper.getPageList(page, null), null);
    }

    @Override
    public void add(SalePriceEntity salePriceEntity) {
        requireNotNull(salePriceEntity, "请求参数不能为空");
        requireNotNull(salePriceEntity.getPrice(), "销售价格不能为空");
        requireNotNull(salePriceEntity.getMaterial(), "销售物料不能为空");
        requireNotNull(salePriceEntity.getCustomer(), "客户不能为空");
        salePriceEntity.setMaterialId(salePriceEntity.getMaterial().getId());
        salePriceEntity.setCustomerId(salePriceEntity.getCustomer().getId());
        salePriceMapper.insert(salePriceEntity);
    }

    @Override
    public SalePriceEntity getById(String id) {
        return salePriceMapper.getById(id);
    }

    @Override
    public void update(SalePriceEntity salePriceEntity) {
        requireNotNull(salePriceEntity, "请求参数不能为空");
        requireNotNull(salePriceEntity.getId(), "id不能为空");
        requireNotNull(salePriceEntity.getPrice(), "销售价格不能为空");
        requireNotNull(salePriceEntity.getMaterial(), "销售物料不能为空");
        requireNotNull(salePriceEntity.getCustomer(), "客户不能为空");
        SalePriceEntity entity = salePriceEntity.builder()
                .materialId(salePriceEntity.getMaterial().getId())
                .customerId(salePriceEntity.getCustomer().getId())
                .price(salePriceEntity.getPrice())
                .build();
        entity.setId(salePriceEntity.getId());
        salePriceMapper.updateById(entity);
    }
}

