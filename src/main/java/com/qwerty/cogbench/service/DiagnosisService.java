package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.Diagnosis;
import org.springframework.security.core.Authentication;

public interface DiagnosisService {

    Diagnosis create(String userEmail, Integer resultId, Diagnosis diagnosis, Authentication authentication);

    Diagnosis update(String userEmail, Integer resultId, Diagnosis diagnosis, Authentication authentication);

    boolean delete(String userEmail, Integer diagnosisId, Authentication authentication);

}
