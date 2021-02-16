package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.Diagnosis;

import java.security.Principal;

public interface DiagnosisService {

  Diagnosis create(Integer resultId, Diagnosis diagnosis,
      Principal principal);

  Diagnosis update(Integer resultId, Diagnosis diagnosis,
      Principal principal);

  boolean delete(Integer diagnosisId, Principal principal);

}
