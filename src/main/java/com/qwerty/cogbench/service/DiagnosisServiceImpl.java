package com.qwerty.cogbench.service;

import com.qwerty.cogbench.exception.ResourceAlreadyExistsException;
import com.qwerty.cogbench.exception.ResourceNotFoundException;
import com.qwerty.cogbench.model.Diagnosis;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.DiagnosisRepository;
import com.qwerty.cogbench.repository.ResultRepository;
import com.qwerty.cogbench.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
public class DiagnosisServiceImpl implements DiagnosisService {

  private final ResultRepository resultRepository;

  private final DiagnosisRepository diagnosisRepository;

  private final UserRepository userRepository;

  public DiagnosisServiceImpl(
      DiagnosisRepository diagnosisRepository,
      ResultRepository resultRepository,
      UserRepository userRepository
  ) {
    this.diagnosisRepository = diagnosisRepository;
    this.resultRepository = resultRepository;
    this.userRepository = userRepository;
  }

  @Override
  public Diagnosis create(
      Integer resultId,
      Diagnosis diagnosis,
      Principal principal
  ) {

    User doctorToFind = userRepository.findUserByEmail(principal.getName()).orElseThrow(() -> {
      String errorMsg = String.format("User [%s] not found", principal.getName());
      log.error(errorMsg);
      return new ResourceNotFoundException(errorMsg);
    });

    Result resultToFind = resultRepository.findResultById(resultId).orElseThrow(() -> {
      String errorMsg = String.format("Result with id [%s] not found", resultId);
      log.error(errorMsg);
      return new ResourceNotFoundException(errorMsg);
    });

    if (diagnosisRepository.findDiagnosisByResult(resultToFind).isPresent()) {
      String errorMsg = String
          .format("Diagnosis for result with id [%s] is already created.", resultId);
      log.error(errorMsg);
      throw new ResourceAlreadyExistsException(errorMsg);
    }

    diagnosis.setDoctor(doctorToFind);
    diagnosis.setResult(resultToFind);

    return diagnosisRepository.save(diagnosis);
  }

  @Override
  public Diagnosis update(Integer resultId, Diagnosis diagnosis,
      Principal principal) {

    User doctorToFind = userRepository.findUserByEmail(principal.getName()).orElseThrow(() -> {
      String errorMsg = String.format("User [%s] not found", principal.getName());
      log.error(errorMsg);
      return new ResourceNotFoundException(errorMsg);
    });

    Result resultToFind = resultRepository.findResultById(resultId).orElseThrow(() -> {
      String errorMsg = String.format("Result with id [%s] not found", resultId);
      log.error(errorMsg);
      return new ResourceNotFoundException(errorMsg);
    });

    Diagnosis diagnosisToFind = diagnosisRepository.findDiagnosisByResult(resultToFind)
        .orElseThrow(() -> {
          String errorMsg = String.format("Diagnosis with result id [%s] not found", resultId);
          log.error(errorMsg);
          return new ResourceNotFoundException(errorMsg);
        });

    diagnosis.setDoctor(doctorToFind);
    diagnosisToFind.setDescription(diagnosis.getDescription());
    diagnosisToFind.setLabel(diagnosis.getLabel());

    return diagnosisRepository.save(diagnosisToFind);
  }

  @Override
  public boolean delete(Integer diagnosisId, Principal principal) {

    Diagnosis diagnosisToFind = diagnosisRepository
        .findDiagnosisById(diagnosisId)
        .orElseThrow(() -> {
          String progressErrorMsg = String
              .format("Diagnosis with Id [%s] not found", diagnosisId);
          log.error(progressErrorMsg);
          throw new ResourceNotFoundException(progressErrorMsg);
        });

    diagnosisRepository.deleteById(diagnosisToFind.getId());

    return true;
  }

}
