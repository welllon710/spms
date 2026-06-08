package com.spms.personal.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.spms.base.PageParam;
import com.spms.base.PageResult;
import com.spms.common.exception.AppException;
import com.spms.personal.entity.UnitEntity;
import com.spms.personal.mapper.UnitMapper;
import com.spms.personal.model.UnitPageFilter;
import com.spms.personal.model.UnitPageRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceImplTest {
    @Mock
    private UnitMapper unitMapper;

    private UnitServiceImpl unitService;

    @BeforeEach
    void setUp() {
        unitService = new UnitServiceImpl(unitMapper);
    }

    @AfterEach
    void tearDown() {
        PageHelper.clearPage();
    }

    @Test
    void getPageTrimsFilterAndReturnsCommonPageResult() {
        Page<UnitEntity> page = new Page<>(1, 20);
        page.setTotal(2);
        page.add(new UnitEntity());
        page.add(new UnitEntity());
        when(unitMapper.getPageList(anyMap())).thenReturn(page);
        UnitPageRequest request = new UnitPageRequest(
                new UnitPageFilter(" meter ", " M ", false),
                new PageParam(1, 20)
        );

        PageResult<UnitEntity> result = unitService.getPage(request);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(unitMapper).getPageList(paramsCaptor.capture());
        assertThat(paramsCaptor.getValue())
                .containsEntry("name", "meter")
                .containsEntry("code", "M")
                .containsEntry("isDisabled", false);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.pageCount()).isEqualTo(1);
        assertThat(result.list()).hasSize(2);
        assertThat(result.page().pageNum()).isEqualTo(1);
        assertThat(result.page().pageSize()).isEqualTo(20);
        assertThat(result.sort().field()).isEqualTo("id");
        assertThat(result.sort().direction()).isEqualTo("desc");
    }

    @Test
    void addTrimsNameAndCodeInitializesBaseEntityAndInserts() {
        when(unitMapper.countByNameOrCode("千克", "KG", null)).thenReturn(0);
        UnitEntity unit = new UnitEntity();
        unit.setName(" 千克 ");
        unit.setCode(" KG ");

        UnitEntity result = unitService.add(unit);

        ArgumentCaptor<UnitEntity> unitCaptor = ArgumentCaptor.forClass(UnitEntity.class);
        verify(unitMapper).insert(unitCaptor.capture());
        assertThat(result.getName()).isEqualTo("千克");
        assertThat(result.getCode()).isEqualTo("KG");
        assertThat(result.getId()).isNull();
        assertThat(result.getCreateTime()).isNotNull();
        assertThat(result.getUpdateTime()).isNotNull();
        assertThat(result.getIsDisabled()).isFalse();
        assertThat(result.getIsPublished()).isFalse();
        assertThat(unitCaptor.getValue().getName()).isEqualTo("千克");
        assertThat(unitCaptor.getValue().getCode()).isEqualTo("KG");
    }

    @Test
    void updateKeepsExistingIsDisabledWhenRequestOmitsItAndReturnsDetail() {
        UnitEntity exist = new UnitEntity();
        exist.setId(1L);
        exist.setName("旧单位");
        exist.setCode("OLD");
        exist.setIsDisabled(true);
        exist.setIsPublished(false);
        UnitEntity updated = new UnitEntity();
        updated.setId(1L);
        updated.setName("新单位");
        updated.setCode("NEW");
        updated.setIsDisabled(true);
        updated.setIsPublished(false);
        when(unitMapper.getById(1L)).thenReturn(exist, updated);
        when(unitMapper.countByNameOrCode("新单位", "NEW", 1L)).thenReturn(0);
        UnitEntity request = new UnitEntity();
        request.setId(1L);
        request.setName(" 新单位 ");
        request.setCode(" NEW ");

        UnitEntity result = unitService.update(request);

        ArgumentCaptor<UnitEntity> unitCaptor = ArgumentCaptor.forClass(UnitEntity.class);
        verify(unitMapper).update(unitCaptor.capture());
        assertThat(unitCaptor.getValue().getIsDisabled()).isTrue();
        assertThat(unitCaptor.getValue().getName()).isEqualTo("新单位");
        assertThat(unitCaptor.getValue().getCode()).isEqualTo("NEW");
        assertThat(unitCaptor.getValue().getUpdateTime()).isNotNull();
        assertThat(result).isSameAs(updated);
    }

    @Test
    void deleteRejectsPublishedUnit() {
        UnitEntity unit = new UnitEntity();
        unit.setId(1L);
        unit.setIsPublished(true);
        when(unitMapper.getById(1L)).thenReturn(unit);

        assertThatThrownBy(() -> unitService.delete(1L))
                .isInstanceOf(AppException.class);

        verify(unitMapper, never()).deleteById(1L);
    }
}
