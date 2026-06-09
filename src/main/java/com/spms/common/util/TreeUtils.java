package com.spms.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class TreeUtils {
    private TreeUtils() {
    }

    public static <T> List<T> buildTree(
            List<T> sourceList,
            Function<T, Long> idGetter,
            Function<T, Long> parentIdGetter,
            BiConsumer<T, List<T>> childrenSetter
    ) {
        return buildTree(sourceList, idGetter, parentIdGetter, childrenSetter, null);
    }

    public static <T> List<T> buildTree(
            List<T> sourceList,
            Function<T, Long> idGetter,
            Function<T, Long> parentIdGetter,
            BiConsumer<T, List<T>> childrenSetter,
            Comparator<T> comparator
    ) {
        if (sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }

        List<T> items = new ArrayList<>(sourceList);
        if (comparator != null) {
            items.sort(comparator);
        }

        Map<Long, T> itemMap = new LinkedHashMap<>();
        Map<Long, List<T>> childrenMap = new LinkedHashMap<>();
        for (T item : items) {
            Long id = idGetter.apply(item);
            itemMap.put(id, item);
            childrenMap.put(id, new ArrayList<>());
        }

        List<T> roots = new ArrayList<>();
        for (T item : items) {
            Long parentId = parentIdGetter.apply(item);
            T parent = itemMap.get(parentId);
            if (Objects.isNull(parentId) || parentId == 0L || Objects.isNull(parent)) {
                roots.add(item);
            } else {
                childrenMap.get(idGetter.apply(parent)).add(item);
            }
        }

        for (T item : items) {
            childrenSetter.accept(item, childrenMap.get(idGetter.apply(item)));
        }
        return roots;
    }

    public static <T> Comparator<T> comparingOrderNoThenId(
            Function<T, Integer> orderNoGetter,
            Function<T, Long> idGetter
    ) {
        return Comparator.comparing(orderNoGetter, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(idGetter, Comparator.nullsLast(Long::compareTo));
    }
}
