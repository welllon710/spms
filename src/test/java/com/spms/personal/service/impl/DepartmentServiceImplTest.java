package com.spms.personal.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.spms.base.PageParam;
import com.spms.base.PageResult;
import com.spms.common.exception.AppException;
import com.spms.personal.entity.DepartmentEntity;
import com.spms.personal.mapper.DepartmentMapper;
import com.spms.personal.model.DepartmentPageFilter;
import com.spms.personal.model.DepartmentPageRequest;
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
class DepartmentServiceImplTest {
    @Mock
    private DepartmentMapper departmentMapper;

    private DepartmentServiceImpl departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentServiceImpl(departmentMapper);
    }

    @AfterEach
    void tearDown() {
        PageHelper.clearPage();
    }

    @Test
    void getPageUsesFilterAndReturnsCommonPageResult() {
        Page<DepartmentEntity> page = new Page<>(1, 20);
        page.setTotal(1);
        page.add(new DepartmentEntity());
        when(departmentMapper.getPageList(anyMap())).thenReturn(page);
        DepartmentPageRequest request = new DepartmentPageRequest(
                new DepartmentPageFilter(" sales ", " D001 ", 0L, false),
                new PageParam(1, 20)
        );

        PageResult<DepartmentEntity> result = departmentService.getPage(request);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(departmentMapper).getPageList(paramsCaptor.capture());
        assertThat(paramsCaptor.getValue())
                .containsEntry("name", "sales")
                .containsEntry("code", "D001")
                .containsEntry("parentId", 0L)
                .containsEntry("isDisabled", false);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.pageCount()).isEqualTo(1);
        assertThat(result.sort().field()).isEqualTo("orderNo");
        assertThat(result.sort().direction()).isEqualTo("asc");
    }

    @Test
    void addInitializesAndInsertsDepartment() {
        when(departmentMapper.countByNameOrCode("sales", "D001", null)).thenReturn(0);
        DepartmentEntity department = new DepartmentEntity();
        department.setName(" sales ");
        department.setCode(" D001 ");
        department.setParentId(0L);
        department.setOrderNo(1);

        DepartmentEntity result = departmentService.add(department);

        ArgumentCaptor<DepartmentEntity> departmentCaptor = ArgumentCaptor.forClass(DepartmentEntity.class);
        verify(departmentMapper).insert(departmentCaptor.capture());
        assertThat(result.getName()).isEqualTo("sales");
        assertThat(result.getCode()).isEqualTo("D001");
        assertThat(result.getIsDisabled()).isFalse();
        assertThat(result.getIsPublished()).isFalse();
        assertThat(departmentCaptor.getValue().getCreateTime()).isNotNull();
    }

    @Test
    void deleteRejectsDepartmentWithChildren() {
        DepartmentEntity department = new DepartmentEntity();
        department.setId(1L);
        department.setIsPublished(false);
        when(departmentMapper.getById(1L)).thenReturn(department);
        when(departmentMapper.countByParentId(1L)).thenReturn(1);

        assertThatThrownBy(() -> departmentService.delete(1L))
                .isInstanceOf(AppException.class);

        verify(departmentMapper, never()).deleteById(1L);
    }
}
