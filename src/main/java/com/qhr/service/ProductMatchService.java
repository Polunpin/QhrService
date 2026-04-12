package com.qhr.service;

import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.match.ApplicationContext;
import com.qhr.vo.match.ProductMatchSummary;

public interface ProductMatchService {

    ProductMatchSummary match(ApplicantProfile applicantProfile, ApplicationContext applicationContext);
}
