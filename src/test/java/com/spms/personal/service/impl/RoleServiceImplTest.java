package com.spms.personal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.spms.common.result.PageResult;
import com.spms.base.PageQuery;
import com.spms.personal.mapper.RoleMapper;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.model.RolePageFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {
    @Mock
    private RoleMapper roleMapper;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(roleMapper);
    }

    @AfterEach
    void tearDown() {
        PageHelper.clearPage();
    }

    @Test
    void getPageUsesCommonRequestAndReturnsCommonPageResult() {
        Page<RoleEntity> page = new Page<>(1, 20);
        page.setTotal(2);
        page.add(new RoleEntity());
        page.add(new RoleEntity());
        when(roleMapper.getPageList(anyMap())).thenReturn(page);
        PageQuery<RolePageFilter> request = new PageQuery<>(
                new RolePageFilter(" admin ", " ADMIN ", false),
                1,
                20
        );

        PageResult<RoleEntity> result = roleService.getPage(request);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(roleMapper).getPageList(paramsCaptor.capture());
        assertThat(paramsCaptor.getValue())
                .containsEntry("name", "admin")
                .containsEntry("code", "ADMIN")
                .containsEntry("isDisabled", false);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.pageCount()).isEqualTo(1);
        assertThat(result.list()).hasSize(2);
        assertThat(result.pageNum()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.sort().field()).isEqualTo("id");
        assertThat(result.sort().direction()).isEqualTo("desc");
    }

    @Test
    void addGeneratesRoleCodeWhenCodeIsBlank() {
        when(roleMapper.getLatestGeneratedRoleCodeForUpdate("RO")).thenReturn("RO0002");
        when(roleMapper.countByNameOrCode("3号", "RO0003", null)).thenReturn(0);
        RoleEntity role = new RoleEntity();
        role.setName("3号");
        role.setCode(" ");

        RoleEntity result = roleService.add(role);

        ArgumentCaptor<RoleEntity> roleCaptor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleMapper).insert(roleCaptor.capture());
        assertThat(result.getCode()).isEqualTo("RO0003");
        assertThat(roleCaptor.getValue().getCode()).isEqualTo("RO0003");
    }

    @Test
    void addKeepsProvidedRoleCode() {
        when(roleMapper.countByNameOrCode("自定义", "CUSTOM", null)).thenReturn(0);
        RoleEntity role = new RoleEntity();
        role.setName("自定义");
        role.setCode(" CUSTOM ");

        RoleEntity result = roleService.add(role);

        verify(roleMapper, never()).getLatestGeneratedRoleCodeForUpdate(anyString());
        ArgumentCaptor<RoleEntity> roleCaptor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleMapper).insert(roleCaptor.capture());
        assertThat(result.getCode()).isEqualTo("CUSTOM");
        assertThat(roleCaptor.getValue().getCode()).isEqualTo("CUSTOM");
    }
}
