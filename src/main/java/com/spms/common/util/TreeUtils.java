package com.spms.common.util;

import com.spms.personal.entity.MenuEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TreeUtils {
    private TreeUtils() {
    }

    public static List<MenuEntity> buildMenuTree(List<MenuEntity> menuList) {
        if (menuList == null || menuList.isEmpty()) {
            return List.of();
        }

        List<MenuEntity> items = new ArrayList<>(menuList);
        items.sort(Comparator.comparing(
                        MenuEntity::getOrderNo,
                        Comparator.nullsLast(Integer::compareTo)
                )
                .thenComparing(MenuEntity::getId, Comparator.nullsLast(Long::compareTo)));

        Map<Long, MenuEntity> itemMap = new LinkedHashMap<>();
        Map<Long, List<MenuEntity>> childrenMap = new LinkedHashMap<>();
        for (MenuEntity item : items) {
            Long id = item.getId();
            itemMap.put(id, item);
            childrenMap.put(id, new ArrayList<>());
        }

        List<MenuEntity> roots = new ArrayList<>();
        for (MenuEntity item : items) {
            Long parentId = item.getParentId();
            MenuEntity parent = itemMap.get(parentId);
            if (Objects.isNull(parentId) || parentId == 0L || Objects.isNull(parent)) {
                roots.add(item);
            } else {
                childrenMap.get(parent.getId()).add(item);
            }
        }

        for (MenuEntity item : items) {
            item.setChildren(childrenMap.get(item.getId()));
        }
        return roots;
    }
}
