package com.qhr.service;

import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.vo.PrecheckResult;

public interface MeasureService {

  PrecheckResult submit(MeasureSubmitRequest request, String openid, String unionid);
}
