package com.qwerty.cogbench.service;

import com.qwerty.cogbench.exception.ResourceAlreadyExistsException;
import com.qwerty.cogbench.exception.ResourceNotFoundException;
import com.qwerty.cogbench.exception.UnauthorizedException;
import com.qwerty.cogbench.model.Diagnosis;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.DiagnosisRepository;
import com.qwerty.cogbench.repository.ResultRepository;
import com.qwerty.cogbench.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
      String userEmail,
      Integer resultId,
      Diagnosis diagnosis,
      Authentication authentication
  ) {

    User doctor = this.isAuthorized(userEmail, authentication);

    Result resultToFind = resultRepository.findResultById(resultId).orElseThrow(() -> {
      String errorMsg = String.format("Result with id [%s] not found", resultId);
      log.error(errorMsg);
      return new ResourceNotFoundException(errorMsg);
    });

    if (diagnosisRepository.findDiagnosisByResult(resultToFind).isPresent()) {
      String errorMsg = String
          .format("Diagnosis for result with id [%s] already created.", resultId);
      log.error(errorMsg);
      throw new ResourceAlreadyExistsException(errorMsg);
    }

    diagnosis.setDoctor(doctor);
    diagnosis.setResult(resultToFind);

    return diagnosisRepository.save(diagnosis);
  }

  @Override
  public Diagnosis update(String userEmail, Integer resultId, Diagnosis diagnosis,
      Authentication authentication) {

    User doctor = this.isAuthorized(userEmail, authentication);

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

    diagnosis.setDoctor(doctor);
    diagnosisToFind.setDescription(diagnosis.getDescription());
    diagnosisToFind.setLabel(diagnosis.getLabel());

    return diagnosisRepository.save(diagnosisToFind);
  }

  @Override
  public boolean delete(String userEmail, Integer diagnosisId, Authentication authentication) {

    this.isAuthorized(userEmail, authentication);

    Diagnosis diagnosisToFind = diagnosisRepository
        .findDiagnosisById(diagnosisId)
        .orElseThrow(() -> {
          String progressErrorMsg = String
              .format("Result for User with userEmail: [%s] and diagnosisId: [%s] not found",
                  userEmail, diagnosisId);
          log.error(progressErrorMsg);
          throw new ResourceNotFoundException(progressErrorMsg);
        });

    diagnosisRepository.deleteById(diagnosisToFind.getId());

    return true;
  }

  /**
   * Check if userEmail is the same as that of that defined in the authentication context.
   * <p>
   * This check is for actions where users are only allowed to modify their own resources.
   *
   * @param userEmail      User email.
   * @param authentication Authentication context containing information of the user submitting the
   *                       request.
   */
  private User isAuthorized(String userEmail, Authentication authentication) {
    String principalName = ((org.springframework.security.core.userdetails.User) authentication
        .getPrincipal()).getUsername();

    User userToFind = userRepository.findUserByEmail(principalName).orElseThrow(
        () -> new ResourceNotFoundException(
            String.format("User [%s] not found", userEmail)));

    if (!userToFind.getRole().equals("ROLE_DOCTOR")) {
      String errorMsg = String.format(
          "User with userEmail: [%s] does not have sufficient permission to modify such resources.",
          principalName);
      throw new UnauthorizedException(errorMsg);
    }
    return userToFind;

  }

}
