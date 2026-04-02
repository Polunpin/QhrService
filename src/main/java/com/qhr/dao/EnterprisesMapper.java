package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EnterprisesMapper extends BaseMapper<Enterprise> {

  List<Enterprise> list(@Param("offset") Integer offset,
                        @Param("size") Integer size);

  List<Enterprise> listByUserId(@Param("openid") String openid,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  int restoreByCreditCode(@Param("creditCode") String creditCode);

}
