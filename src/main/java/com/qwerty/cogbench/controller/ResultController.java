package com.qwerty.cogbench.controller;

import com.qwerty.cogbench.dto.ResultDistriDto;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.service.ResultService;
import com.qwerty.cogbench.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(
    value = {"/result"},
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class ResultController {

  private final ResultService resultService;
  private final UserService userService;

  public ResultController(ResultService resultService, UserService userService) {
    this.resultService = resultService;
    this.userService = userService;
  }

  /**
   * Fetch all user results.
   *
   * @param principal Principal context containing information of the user submitting the request
   * @return Listed result of all results
   */
  @RequestMapping(method = RequestMethod.GET, path = "/patients")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public List<User> fetchAllPatients(Principal principal) {
    log.info("Fetching all patients using user with Id [{}]", principal.getName());
    return userService.fetchAllPatients("ROLE_PATIENT");
  }

  /**
   * Fetch a user's results.
   *
   * @return Paginated result of all results
   */
  @RequestMapping(method = RequestMethod.GET, path = "/patients/{userEmail}")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public List<Result> fetchPatientResults(
          @PathVariable(value = "userEmail") String userEmail) {
    log.info("Fetching results of patient with Id [{}]", userEmail);
    return resultService.fetchResultsWithUserEmail(userEmail);
  }

  /**
   * Fetch all user results.
   *
   * @param principal Principal context containing information of the user submitting the request
   * @return Paginated result of all results
   */
  @RequestMapping(method = RequestMethod.GET, path = "/")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public List<Result> fetchAllResults(Principal principal) {
    log.info("Fetching all results using user with Id [{}]", principal.getName());
    return resultService.fetchAll();
  }

  /**
   * Get result.
   *
   * @param principal Principal context containing information of the user submitting the request
   * @return Result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/latest")
  @Secured({"ROLE_PATIENT"})
  @ResponseStatus(HttpStatus.OK)
  public Result getLatestUserResult(
      Principal principal
  ) {
    log.info("Getting latest result for user [{}]", principal.getName());
    return resultService.getLatestResult(principal);
  }

  /**
   * Get result.
   *
   * @param principal Principal context containing information of the user submitting the request
   * @return Result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/me")
  @Secured({"ROLE_PATIENT"})
  @ResponseStatus(HttpStatus.OK)
  public List<Result> fetchAllUserResult(
      Principal principal
  ) {
    log.info("Getting all result for user [{}]", principal.getName());
    return resultService.getHistory(principal);
  }

  /**
   * Create a new result.
   *
   * @param result Result to be created
   * @param principal Principal context containing information of the user submitting the request
   * @return Created result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/create")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public Result createResult(
      @RequestBody Result result,
      Principal principal
  ) {
    log.info("Creating result for user [{}] with user [{}]", result.getUser().getEmail(), principal.getName());
    return resultService.create(result, principal);
  }

  /**
   * Update result.
   *
   * @param resultId Id of result to update
   * @param principal Principal context containing information of the user submitting the request
   * @return Created result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/{resultId}")
  @Secured({"ROLE_DOCTOR", "ROLE_PATIENT"})
  @ResponseStatus(HttpStatus.OK)
  public Result fetchResult(
          @PathVariable(value = "resultId") Integer resultId,
          Principal principal
  ) {
    log.info("Fetching result with Id [{}] for user with Id [{}]", resultId, principal.getName());
    return resultService.fetch(resultId, principal);
  }

  /**
   * Update result.
   *
   * @param resultId Id of result to update
   * @param result Result to be created
   * @param principal Principal context containing information of the user submitting the request
   * @return Created result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/update/{resultId}")
  @Secured({"ROLE_PATIENT", "ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public Result updateResult(
          @PathVariable(value = "resultId") Integer resultId,
          @RequestBody Result result,
          Principal principal
  ) {
    log.info("Updating result with Id [{}] for user with Id [{}]", resultId, principal.getName());
    return resultService.update(resultId, result, principal);
  }

  /**
   * Delete a Result.
   * <p>
   *
   * @param resultId       Result id of result to be deleted
   * @param principal Principal context containing information of the user submitting the request
   * @return Flag indicating if request is successful
   */
  @RequestMapping(method = RequestMethod.DELETE, path = "/delete/{resultId}")
  @ResponseStatus(HttpStatus.OK)
  @Secured({"ROLE_DOCTOR"})
  public boolean deleteResult(
      @PathVariable(value = "resultId") Integer resultId,
      Principal principal
  ) {
    log.info("Deleting result with id [{}] for user [{}]", resultId, principal.getName());

    return resultService.delete(resultId, principal);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/graph/time")
  @ResponseStatus(HttpStatus.OK)
  @Secured({"ROLE_DOCTOR", "ROLE_PATIENT"})
  public ResultDistriDto getTimeGraphData(
      @RequestParam(value = "bins", defaultValue="10") Integer bins,
      @RequestParam(value = "nodeNum", defaultValue="25") Integer nodeNum
  ) {
    log.info("Fetching graph data for result.time");

    return resultService.getTimeGraphData(bins, nodeNum);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/graph/accuracy")
  @ResponseStatus(HttpStatus.OK)
  @Secured({"ROLE_DOCTOR", "ROLE_PATIENT"})
  public ResultDistriDto getAccuracyGraphData(
      @RequestParam(value = "bins", defaultValue="10") Integer bins,
      @RequestParam(value = "nodeNum", defaultValue="25") Integer nodeNum
  ) {
    log.info("Fetching graph data for result.accuracy");

    return resultService.getAccuracyGraphData(bins, nodeNum);
  }


}
