package com.spms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.system.entity.CodeRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeRuleMapper extends BaseMapper<CodeRuleEntity> {
    CodeRuleEntity selectByRuleFieldForUpdate(@Param("ruleField") Integer ruleField);
}
