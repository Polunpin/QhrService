package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditProductsMapper extends BaseMapper<Product> {

  List<Product> list(@Param("offset") Integer offset,
                     @Param("size") Integer size);

}
