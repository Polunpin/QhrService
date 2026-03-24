package com.qhr.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface QccClientService {

  /*企业模糊查询*/
  JsonNode getList(String searchKey);
}
