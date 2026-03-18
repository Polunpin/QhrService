package com.qhr.service;

import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.Person;

public interface DmnDecisionService {

  Object precheck(Person request);

  Object match(ApplicantProfile applicantProfile);

}
