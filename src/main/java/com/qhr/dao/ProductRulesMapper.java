package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.ProductRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductRulesMapper extends BaseMapper<ProductRule> {

    List<ProductRule> list(@Param("offset") Integer offset,
                           @Param("size") Integer size);
}
