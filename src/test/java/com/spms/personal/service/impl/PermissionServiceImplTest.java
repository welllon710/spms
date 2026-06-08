package com.spms.personal.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.spms.base.PageParam;
import com.spms.base.PageResult;
import com.spms.common.exception.AppException;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.mapper.PermissionMapper;
import com.spms.personal.model.PermissionPageFilter;
import com.spms.personal.model.PermissionPageRequest;
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
class PermissionServiceImplTest {
    @Mock
    private PermissionMapper permissionMapper;

    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionServiceImpl(permissionMapper);
    }

    @AfterEach
    void tearDown() {
        PageHelper.clearPage();
    }

    @Test
    void getPageUsesFilterAndReturnsCommonPageResult() {
        Page<PermissionEntity> page = new Page<>(1, 20);
        page.setTotal(1);
        page.add(new PermissionEntity());
        when(permissionMapper.getPageList(anyMap())).thenReturn(page);
        PermissionPageRequest request = new PermissionPageRequest(
                new PermissionPageFilter(" role:add ", " add ", 0L, 1, false, false),
                new PageParam(1, 20)
        );

        PageResult<PermissionEntity> result = permissionService.getPage(request);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(permissionMapper).getPageList(paramsCaptor.capture());
        assertThat(paramsCaptor.getValue())
                .containsEntry("identity", "role:add")
                .containsEntry("name", "add")
                .containsEntry("parentId", 0L)
                .containsEntry("type", 1)
                .containsEntry("isSystem", false)
                .containsEntry("isDisabled", false);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.pageCount()).isEqualTo(1);
        assertThat(result.sort().field()).isEqualTo("id");
        assertThat(result.sort().direction()).isEqualTo("asc");
    }

    @Test
    void addInitializesAndInsertsPermission() {
        when(permissionMapper.countByIdentityOrName("role:add", "新增角色", null)).thenReturn(0);
        PermissionEntity permission = new PermissionEntity();
        permission.setIdentity(" role:add ");
        permission.setName(" 新增角色 ");
        permission.setParentId(0L);
        permission.setType(1);

        PermissionEntity result = permissionService.add(permission);

        ArgumentCaptor<PermissionEntity> permissionCaptor = ArgumentCaptor.forClass(PermissionEntity.class);
        verify(permissionMapper).insert(permissionCaptor.capture());
        assertThat(result.getIdentity()).isEqualTo("role:add");
        assertThat(result.getName()).isEqualTo("新增角色");
        assertThat(result.getIsDisabled()).isFalse();
        assertThat(result.getIsPublished()).isFalse();
        assertThat(result.getIsSystem()).isFalse();
        assertThat(permissionCaptor.getValue().getCreateTime()).isNotNull();
    }

    @Test
    void deleteRejectsSystemPermission() {
        PermissionEntity permission = new PermissionEntity();
        permission.setId(1L);
        permission.setIsPublished(false);
        permission.setIsSystem(true);
        when(permissionMapper.getById(1L)).thenReturn(permission);

        assertThatThrownBy(() -> permissionService.delete(1L))
                .isInstanceOf(AppException.class);

        verify(permissionMapper, never()).deleteById(1L);
    }
}
