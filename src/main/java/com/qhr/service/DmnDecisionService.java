package com.qhr.service;

import com.qhr.vo.Person;

public interface DmnDecisionService {

  Object evaluate(Person request);
}
