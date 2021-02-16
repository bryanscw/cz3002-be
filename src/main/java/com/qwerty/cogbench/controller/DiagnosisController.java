package com.qwerty.cogbench.controller;

import com.qwerty.cogbench.model.Diagnosis;
import com.qwerty.cogbench.service.DiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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
   * @param resultId       Id of the result of which the diagnosis should be created for
   * @param diagnosis      Diagnosis to be added
   * @param principal Principal context containing information of the user submitting the request
   * @return Created result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/create/{resultId}")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public Diagnosis createDiagnosis(
      @PathVariable(value = "resultId") Integer resultId,
      @RequestBody Diagnosis diagnosis,
      Principal principal
  ) {
    log.info("Creating diagnosis for report with id: [{}]", resultId);
    return diagnosisService.create(resultId, diagnosis, principal);
  }

  /**
   * Create a new diagnosis.
   *
   * @param diagnosis      Diagnosis to be added
   * @param principal Principal context containing information of the user submitting the request
   * @return Created result
   */
  @RequestMapping(method = {RequestMethod.POST,
      RequestMethod.PATCH}, path = "/update/{resultId}")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public Diagnosis updateDiagnosis(
      @PathVariable(value = "resultId") Integer resultId,
      @RequestBody Diagnosis diagnosis,
      Principal principal
  ) {
    log.info("Updating diagnosis for result with Id [{}]", resultId);
    return diagnosisService.update(resultId, diagnosis, principal);
  }

  /**
   * Delete a Diagnosis.
   * <p>
   *
   * @param diagnosisId    Diagnosis id of diagnosis to be deleted
   * @param principal Principal context containing information of the user submitting the request
   * @return Flag indicating if request is successful
   */
  @RequestMapping(method = RequestMethod.DELETE, path = "/delete/{diagnosisId}")
  @ResponseStatus(HttpStatus.OK)
  @Secured({"ROLE_DOCTOR"})
  public boolean deleteDiagnosis(
      @PathVariable(value = "diagnosisId") Integer diagnosisId,
      Principal principal
  ) {
    log.info("Deleting diagnosis with Id [{}]", diagnosisId);
    return diagnosisService.delete(diagnosisId, principal);
  }

}
