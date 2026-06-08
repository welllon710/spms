package com.spms.base;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Supplier;

public abstract class BaseService {
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    protected <T> PageInfo<T> getPage(PageRequest request, Supplier<List<T>> query) {
        PageHelper.startPage(getPageNum(request), getPageSize(request));
        return new PageInfo<>(query.get());
    }

    protected void initAddEntity(BaseEntity entity) {
        requireEntity(entity);
        long now = System.currentTimeMillis();
        entity.setId(null);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDisabled(Boolean.TRUE.equals(entity.getIsDisabled()));
        entity.setIsPublished(false);
    }

    protected void initUpdateEntity(BaseEntity entity) {
        requireEntity(entity);
        entity.setUpdateTime(System.currentTimeMillis());
    }

    protected void checkEditable(BaseEntity entity) {
        requireEntity(entity);
        if (Boolean.TRUE.equals(entity.getIsPublished())) {
            throw new AppException(CommonError.FORBIDDEN, "无法修改或删除已经发布的数据");
        }
    }

    protected void requireEntity(Object entity) {
        if (entity == null) {
            throw new AppException(CommonError.PARAM_MISSING, "请求参数不能为空");
        }
    }

    protected void requireId(Long id, String message) {
        if (id == null) {
            throw new AppException(CommonError.PARAM_MISSING, message);
        }
    }

    protected void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(CommonError.PARAM_MISSING, message);
        }
    }

    protected String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private int getPageNum(PageRequest request) {
        if (request == null || request.pageNum() == null || request.pageNum() < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return request.pageNum();
    }

    private int getPageSize(PageRequest request) {
        if (request == null || request.pageSize() == null || request.pageSize() < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(request.pageSize(), MAX_PAGE_SIZE);
    }
}
