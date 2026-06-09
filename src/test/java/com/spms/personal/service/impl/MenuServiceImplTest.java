package com.spms.personal.service.impl;

import com.spms.base.PageQuery;
import com.spms.common.exception.AppException;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.mapper.MenuMapper;
import com.spms.personal.model.MenuPageFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {
    @Mock
    private MenuMapper menuMapper;

    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuServiceImpl(menuMapper);
    }

    @Test
    void getPageUsesFilterAndReturnsMenuTree() {
        MenuEntity menu = new MenuEntity();
        menu.setId(1L);
        menu.setParentId(0L);
        menu.setOrderNo(1);
        when(menuMapper.getPageList(anyMap())).thenReturn(List.of(menu));
        PageQuery<MenuPageFilter> request = new PageQuery<>(
                new MenuPageFilter(" system ", 0L, "/system", " Layout ", false),
                1,
                20
        );

        List<MenuEntity> result = menuService.getPage(request);

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(menuMapper).getPageList(paramsCaptor.capture());
        assertThat(paramsCaptor.getValue())
                .containsEntry("name", "system")
                .containsEntry("parentId", 0L)
                .containsEntry("path", "/system")
                .containsEntry("component", "Layout")
                .containsEntry("isDisabled", false);
        assertThat(result).containsExactly(menu);
        assertThat(result.get(0).getChildren()).isEmpty();
    }

    @Test
    void addInitializesAndInsertsMenu() {
        when(menuMapper.countByName("system", null)).thenReturn(0);
        MenuEntity menu = new MenuEntity();
        menu.setName(" system ");
        menu.setPath(" /system ");
        menu.setComponent(" Layout ");
        menu.setIcon(" settings ");
        menu.setParentId(0L);
        menu.setOrderNo(1);

        MenuEntity result = menuService.add(menu);

        ArgumentCaptor<MenuEntity> menuCaptor = ArgumentCaptor.forClass(MenuEntity.class);
        verify(menuMapper).insert(menuCaptor.capture());
        assertThat(result.getName()).isEqualTo("system");
        assertThat(result.getPath()).isEqualTo("/system");
        assertThat(result.getComponent()).isEqualTo("Layout");
        assertThat(result.getIcon()).isEqualTo("settings");
        assertThat(result.getIsDisabled()).isFalse();
        assertThat(result.getIsPublished()).isFalse();
        assertThat(menuCaptor.getValue().getCreateTime()).isNotNull();
    }

    @Test
    void deleteRejectsMenuWithChildren() {
        MenuEntity menu = new MenuEntity();
        menu.setId(1L);
        menu.setIsPublished(false);
        when(menuMapper.getById(1L)).thenReturn(menu);
        when(menuMapper.countByParentId(1L)).thenReturn(1);

        assertThatThrownBy(() -> menuService.delete(1L))
                .isInstanceOf(AppException.class);

        verify(menuMapper, never()).deleteById(1L);
    }
}
