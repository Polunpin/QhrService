package com.qhr.service;

import com.qhr.dto.MeasureSubmitRequest;

public interface MeasureService {

  Object submit(MeasureSubmitRequest request, String openid, String unionid);
}
