package com.spms.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.asset.entity.MaterialEntity;
import com.spms.asset.mapper.MaterialMapper;
import com.spms.asset.model.MaterialPageFilter;
import com.spms.base.PageQuery;
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
class MaterialServiceImplTest {
    @Mock
    private MaterialMapper materialMapper;

    private MaterialServiceImpl materialService;

    @BeforeEach
    void setUp() {
        materialService = new MaterialServiceImpl(materialMapper);
    }

    @Test
    void getPageBuildsWrapperAndReturnsCommonPageResult() {
        when(materialMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<MaterialEntity> page = invocation.getArgument(0);
            page.setTotal(2);
            page.setRecords(List.of(new MaterialEntity(), new MaterialEntity()));
            return page;
        });
        PageQuery<MaterialPageFilter> request = new PageQuery<>(
                new MaterialPageFilter(" steel ", " M001 ", " 10mm ", 1L, 2L, 3L, false),
                1,
                20
        );

        PageResult<MaterialEntity> result = materialService.getPage(request);

        ArgumentCaptor<Page<MaterialEntity>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Wrapper<MaterialEntity>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(materialMapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
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
        when(materialMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        MaterialEntity material = new MaterialEntity();
        material.setId(99L);
        material.setName(" Steel ");
        material.setCode(" M001 ");
        material.setSpc(" 10mm ");

        MaterialEntity result = materialService.add(material);

        ArgumentCaptor<MaterialEntity> materialCaptor = ArgumentCaptor.forClass(MaterialEntity.class);
        verify(materialMapper).insert(materialCaptor.capture());
        assertThat(result.getName()).isEqualTo("Steel");
        assertThat(result.getCode()).isEqualTo("M001");
        assertThat(result.getSpc()).isEqualTo("10mm");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreateTime()).isNotNull();
        assertThat(result.getUpdateTime()).isNotNull();
        assertThat(result.getIsDisabled()).isFalse();
        assertThat(result.getIsPublished()).isFalse();
        assertThat(materialCaptor.getValue().getName()).isEqualTo("Steel");
    }

    @Test
    void updateKeepsExistingIsDisabledWhenRequestOmitsItAndReturnsDetail() {
        MaterialEntity exist = new MaterialEntity();
        exist.setId(1L);
        exist.setName("Old");
        exist.setCode("OLD");
        exist.setIsDisabled(true);
        exist.setIsPublished(false);
        MaterialEntity updated = new MaterialEntity();
        updated.setId(1L);
        updated.setName("New");
        updated.setCode("NEW");
        updated.setIsDisabled(true);
        updated.setIsPublished(false);
        when(materialMapper.selectById(1L)).thenReturn(exist, updated);
        when(materialMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        MaterialEntity request = new MaterialEntity();
        request.setId(1L);
        request.setName(" New ");
        request.setCode(" NEW ");

        MaterialEntity result = materialService.update(request);

        ArgumentCaptor<MaterialEntity> materialCaptor = ArgumentCaptor.forClass(MaterialEntity.class);
        verify(materialMapper).updateById(materialCaptor.capture());
        assertThat(materialCaptor.getValue().getIsDisabled()).isTrue();
        assertThat(materialCaptor.getValue().getName()).isEqualTo("New");
        assertThat(materialCaptor.getValue().getCode()).isEqualTo("NEW");
        assertThat(materialCaptor.getValue().getUpdateTime()).isNotNull();
        assertThat(result).isSameAs(updated);
    }

    @Test
    void deleteRejectsPublishedMaterial() {
        MaterialEntity material = new MaterialEntity();
        material.setId(1L);
        material.setIsPublished(true);
        when(materialMapper.selectById(1L)).thenReturn(material);

        assertThatThrownBy(() -> materialService.delete(1L))
                .isInstanceOf(AppException.class);

        verify(materialMapper, never()).deleteById(1L);
    }
}
