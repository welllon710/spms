package com.spms.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.mapper.CustomerMapper;
import com.spms.channel.model.CustomerPageFilter;
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
class CustomerServiceImplTest {
    @Mock
    private CustomerMapper customerMapper;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerMapper);
    }

    @Test
    void getPageTrimsFilterAndReturnsCommonPageResult() {
        when(customerMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<CustomerEntity> page = invocation.getArgument(0);
            page.setTotal(2);
            page.setRecords(List.of(new CustomerEntity(), new CustomerEntity()));
            return page;
        });
        PageQuery<CustomerPageFilter> request = new PageQuery<>(
                new CustomerPageFilter(" acme ", " C001 ", " 138 ", false),
                1,
                20
        );

        PageResult<CustomerEntity> result = customerService.getPage(request);

        ArgumentCaptor<Page<CustomerEntity>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Wrapper<CustomerEntity>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(customerMapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
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
    void addTrimsFieldsClearsIdInitializesBaseEntityAndInserts() {
        when(customerMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        CustomerEntity customer = new CustomerEntity();
        customer.setId(99L);
        customer.setName(" Acme ");
        customer.setCode(" C001 ");
        customer.setPhone(" 13800000000 ");

        CustomerEntity result = customerService.add(customer);

        ArgumentCaptor<CustomerEntity> customerCaptor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerMapper).insert(customerCaptor.capture());
        assertThat(result.getName()).isEqualTo("Acme");
        assertThat(result.getCode()).isEqualTo("C001");
        assertThat(result.getPhone()).isEqualTo("13800000000");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreateTime()).isNotNull();
        assertThat(result.getUpdateTime()).isNotNull();
        assertThat(result.getIsDisabled()).isFalse();
        assertThat(result.getIsPublished()).isFalse();
        assertThat(customerCaptor.getValue().getName()).isEqualTo("Acme");
    }

    @Test
    void updateKeepsExistingIsDisabledWhenRequestOmitsItAndReturnsDetail() {
        CustomerEntity exist = new CustomerEntity();
        exist.setId(1L);
        exist.setName("Old");
        exist.setCode("OLD");
        exist.setIsDisabled(true);
        exist.setIsPublished(false);
        CustomerEntity updated = new CustomerEntity();
        updated.setId(1L);
        updated.setName("New");
        updated.setCode("NEW");
        updated.setIsDisabled(true);
        updated.setIsPublished(false);
        when(customerMapper.selectById(1L)).thenReturn(exist, updated);
        when(customerMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        CustomerEntity request = new CustomerEntity();
        request.setId(1L);
        request.setName(" New ");
        request.setCode(" NEW ");

        CustomerEntity result = customerService.update(request);

        ArgumentCaptor<CustomerEntity> customerCaptor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerMapper).updateById(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getIsDisabled()).isTrue();
        assertThat(customerCaptor.getValue().getName()).isEqualTo("New");
        assertThat(customerCaptor.getValue().getCode()).isEqualTo("NEW");
        assertThat(customerCaptor.getValue().getUpdateTime()).isNotNull();
        assertThat(result).isSameAs(updated);
    }

    @Test
    void deleteRejectsPublishedCustomer() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setIsPublished(true);
        when(customerMapper.selectById(1L)).thenReturn(customer);

        assertThatThrownBy(() -> customerService.delete(1L))
                .isInstanceOf(AppException.class);

        verify(customerMapper, never()).deleteById(1L);
    }
}
