package com.spms.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.channel.entity.CustomerEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<CustomerEntity> {
}
