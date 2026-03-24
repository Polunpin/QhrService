package com.qhr.service;

import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.dto.MeasureSubmitResponse;

public interface MeasureService {

  MeasureSubmitResponse submit(MeasureSubmitRequest request, String openid, String unionid);
}
