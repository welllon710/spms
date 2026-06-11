package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.PageQuery;
import com.spms.channel.entity.SupplierEntity;
import com.spms.channel.mapper.SupplierMapper;
import com.spms.channel.model.SupplierPageFilter;
import com.spms.common.exception.AppException;
import com.spms.common.result.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {
    @Mock
    private SupplierMapper supplierMapper;

    private SupplierServiceImpl supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierServiceImpl(supplierMapper);
    }

    @Test
    void getPageTrimsFilterAndReturnsCommonPageResult() {
        when(supplierMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<SupplierEntity> page = invocation.getArgument(0);
            page.setTotal(2);
            page.setRecords(List.of(new SupplierEntity(), new SupplierEntity()));
            return page;
        });
        PageQuery<SupplierPageFilter> request = new PageQuery<>(
                new SupplierPageFilter(" acme ", " S001 ", " 138 ", false),
                1,
                20
        );

        PageResult<SupplierEntity> result = supplierService.getPage(request);

        ArgumentCaptor<Page<SupplierEntity>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Wrapper<SupplierEntity>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(supplierMapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20);
        assertThat(wrapperCaptor.getValue()).isNotNull();
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.pageCount()).isEqualTo(1);
        assertThat(result.list()).hasSize(2);
        assertThat(result.sort().field()).isEqualTo("id");
        assertThat(result.sort().direction()).isEqualTo("desc");
    }

    @Test
    void addTrimsFieldsInitializesBaseEntityAndInserts() {
        when(supplierMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        SupplierEntity supplier = new SupplierEntity();
        supplier.setName(" Acme ");
        supplier.setCode(" S001 ");
        supplier.setPhone(" 13800000000 ");

        SupplierEntity result = supplierService.add(supplier);

        ArgumentCaptor<SupplierEntity> supplierCaptor = ArgumentCaptor.forClass(SupplierEntity.class);
        verify(supplierMapper).insert(supplierCaptor.capture());
        assertThat(result.getName()).isEqualTo("Acme");
        assertThat(result.getCode()).isEqualTo("S001");
        assertThat(result.getPhone()).isEqualTo("13800000000");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreateTime()).isNotNull();
        assertThat(result.getUpdateTime()).isNotNull();
        assertThat(result.getIsDisabled()).isFalse();
        assertThat(result.getIsPublished()).isFalse();
        assertThat(supplierCaptor.getValue().getName()).isEqualTo("Acme");
    }

    @Test
    void updateKeepsExistingIsDisabledWhenRequestOmitsItAndReturnsDetail() {
        SupplierEntity exist = new SupplierEntity();
        exist.setId(1L);
        exist.setName("Old");
        exist.setCode("OLD");
        exist.setIsDisabled(true);
        exist.setIsPublished(false);
        SupplierEntity updated = new SupplierEntity();
        updated.setId(1L);
        updated.setName("New");
        updated.setCode("NEW");
        updated.setIsDisabled(true);
        updated.setIsPublished(false);
        when(supplierMapper.selectById(1L)).thenReturn(exist, updated);
        when(supplierMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        SupplierEntity request = new SupplierEntity();
        request.setId(1L);
        request.setName(" New ");
        request.setCode(" NEW ");

        SupplierEntity result = supplierService.update(request);

        ArgumentCaptor<SupplierEntity> supplierCaptor = ArgumentCaptor.forClass(SupplierEntity.class);
        verify(supplierMapper).updateById(supplierCaptor.capture());
        assertThat(supplierCaptor.getValue().getIsDisabled()).isTrue();
        assertThat(supplierCaptor.getValue().getName()).isEqualTo("New");
        assertThat(supplierCaptor.getValue().getCode()).isEqualTo("NEW");
        assertThat(supplierCaptor.getValue().getUpdateTime()).isNotNull();
        assertThat(result).isSameAs(updated);
    }

    @Test
    void deleteRejectsPublishedSupplier() {
        SupplierEntity supplier = new SupplierEntity();
        supplier.setId(1L);
        supplier.setIsPublished(true);
        when(supplierMapper.selectById(1L)).thenReturn(supplier);

        assertThatThrownBy(() -> supplierService.delete(1L))
                .isInstanceOf(AppException.class);

        verify(supplierMapper, never()).deleteById(1L);
    }
}
