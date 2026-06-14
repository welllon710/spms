package com.spms.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class EntityMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        long now = System.currentTimeMillis();
        this.strictInsertFill(metaObject, "createTime", () -> now, Long.class);
        this.strictInsertFill(metaObject, "updateTime", () -> now, Long.class);
        this.strictInsertFill(metaObject, "isDisabled", () -> false, Boolean.class);
        this.strictInsertFill(metaObject, "isPublished", () -> false, Boolean.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", System::currentTimeMillis, Long.class);
    }
}
