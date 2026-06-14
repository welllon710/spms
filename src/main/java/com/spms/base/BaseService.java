package com.spms.base;

import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;

public abstract class BaseService<E extends BaseEntity> {
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    protected void checkEditable(E entity) {
        if (Boolean.TRUE.equals(entity.getIsPublished())) {
            throw new AppException(CommonError.FORBIDDEN, "无法修改或删除已经发布的数据");
        }
    }

    protected int getPageNum(PageQuery<?> request) {
        Integer pageNum = getRequestPageNum(request);
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    protected int getPageSize(PageQuery<?> request) {
        Integer pageSize = getRequestPageSize(request);
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private Integer getRequestPageNum(PageQuery<?> request) {
        if (request == null) {
            return null;
        }
        if (request.pageNum() != null) {
            return request.pageNum();
        }
        return request.page() == null ? null : request.page().pageNum();
    }

    private Integer getRequestPageSize(PageQuery<?> request) {
        if (request == null) {
            return null;
        }
        if (request.pageSize() != null) {
            return request.pageSize();
        }
        return request.page() == null ? null : request.page().pageSize();
    }

}
