package com.qwerty.cogbench.controller;

import com.qwerty.cogbench.model.Diagnosis;
import com.qwerty.cogbench.service.DiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(
    value = {"/diagnosis"},
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class DiagnosisController {

  private final DiagnosisService diagnosisService;

  public DiagnosisController(DiagnosisService diagnosisService) {
    this.diagnosisService = diagnosisService;
  }

  /**
   * Create a new diagnosis.
   *
   * @param userEmail      Email of doctor creating the diagnosis
   * @param resultId       Id of the result of which the diagnosis should be created for
   * @param diagnosis      Diagnosis to be added
   * @param authentication Authentication context containing information of the user submitting the
   *                       request
   * @return Created result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/{userEmail}/create/{resultId}")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public Diagnosis createDiagnosis(
      @PathVariable(value = "userEmail") String userEmail,
      @PathVariable(value = "resultId") Integer resultId,
      @RequestBody Diagnosis diagnosis,
      Authentication authentication
  ) {
    log.info("Creating diagnosis for report with id: [{}]", resultId);
    return diagnosisService.create(userEmail, resultId, diagnosis, authentication);
  }

  /**
   * Create a new diagnosis.
   *
   * @param userEmail      Email of doctor creating the diagnosis
   * @param diagnosis      Diagnosis to be added
   * @param authentication Authentication context containing information of the user submitting the
   *                       request
   * @return Created result
   */
  @RequestMapping(method = {RequestMethod.POST,
      RequestMethod.PATCH}, path = "/{userEmail}/update/{resultId}")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public Diagnosis updateDiagnosis(
      @PathVariable(value = "userEmail") String userEmail,
      @PathVariable(value = "resultId") Integer resultId,
      @RequestBody Diagnosis diagnosis,
      Authentication authentication
  ) {
    log.info("Updating diagnosis for user [{}] with result Id [{}]", userEmail, resultId);
    return diagnosisService.update(userEmail, resultId, diagnosis, authentication);
  }

  /**
   * Delete a Diagnosis.
   * <p>
   *
   * @param userEmail      Email of doctor deleting the result
   * @param diagnosisId    Diagnosis id of diagnosis to be deleted
   * @param authentication Authentication context containing information of the user submitting the
   *                       request
   * @return Flag indicating if request is successful
   */
  @RequestMapping(method = RequestMethod.DELETE, path = "/{userEmail}/delete/{diagnosisId}")
  @ResponseStatus(HttpStatus.OK)
  @Secured({"ROLE_DOCTOR"})
  public boolean deleteDiagnosis(
      @PathVariable(value = "userEmail") String userEmail,
      @PathVariable(value = "diagnosisId") Integer diagnosisId,
      Authentication authentication
  ) {
    log.info("Deleting diagnosis with id [{}] for user [{}]", diagnosisId, userEmail);
    return diagnosisService.delete(userEmail, diagnosisId, authentication);
  }

}
