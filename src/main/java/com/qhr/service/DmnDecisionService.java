package com.qhr.service;

import com.qhr.dto.EnterprisePayload;
import com.qhr.vo.ApplicantProfile;

public interface DmnDecisionService {

  Object precheck(EnterprisePayload request);

  Object match(ApplicantProfile applicantProfile);

}
