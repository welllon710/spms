package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.PurchaseDetailEntity;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.entity.PurchasePriceEntity;
import com.spms.channel.mapper.PurchaseDetailMapper;
import com.spms.channel.mapper.PurchaseMapper;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.service.PurchaseService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireNotNull;

@Service
@AllArgsConstructor
public class PurchaseServiceImpl extends BaseService<PurchaseEntity> implements PurchaseService {

    private final PurchaseMapper purchaseMapper;

    private final PurchaseDetailMapper purchaseDetailMapper;

    @Override
    public PageResult<PurchaseEntity> getPage(@RequestBody PageQuery<PurchasePageFilter> request) {
        PurchasePageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .put("billCode", PurchasePageFilter::billCode)
                .put("status", PurchasePageFilter::status)
                .toMap();
        LambdaQueryWrapper<PurchaseEntity> wrapper = Wrappers.lambdaQuery(PurchaseEntity.class);
        wrapper.eq(PurchaseEntity::getStatus, params.get("status"))
                .eq(PurchaseEntity::getBillCode, params.get("billCode"))
                .orderByDesc(PurchaseEntity::getId);
        Page<PurchaseEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(purchaseMapper.selectPage(page, wrapper), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(PurchasePageFilter request) {
        requireNotNull(request, "请求参数不能为空");
        //新增采购单
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setReason(request.reason())
                .setBillCode(request.billCode());
        initAddEntity(purchaseEntity);
        purchaseMapper.insert(purchaseEntity);
        Long billId = purchaseEntity.getId();

        List<PurchaseDetailEntity> details = request.details();
        ArrayList<PurchaseDetailEntity> list = new ArrayList<>();
        requireNotNull(details, "采购明细不能为空");
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
    };

}
