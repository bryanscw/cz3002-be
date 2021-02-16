package com.qwerty.cogbench.controller;

import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.service.ResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    value = {"/result"},
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class ResultController {

  private final ResultService resultService;

  public ResultController(ResultService resultService) {
    this.resultService = resultService;
  }

  /**
   * Fetch all user results.
   *
   * @param pageable Pagination context
   * @return Paginated result of all results
   */
  @RequestMapping(method = RequestMethod.GET, path = "/")
  @Secured({"ROLE_DOCTOR"})
  @ResponseStatus(HttpStatus.OK)
  public Page<Result> fetchAllResults(Pageable pageable) {
    log.info("Fetching all results with pagination context: [{}]", pageable.toString());
    return resultService.fetchAll(pageable);
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
   * @param pageable       Pagination context
   * @param principal Principal context containing information of the user submitting the request
   * @return Result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/all")
  @Secured({"ROLE_PATIENT"})
  @ResponseStatus(HttpStatus.OK)
  public Page<Result> fetchAllUserResult(
      Pageable pageable,
      Principal principal
  ) {
    log.info("Getting all result for user [{}]", principal.getName());
    return resultService.getHistory(pageable, principal);
  }

  /**
   * Create a new result.
   *
   * @param result Result to be created
   * @param principal Principal context containing information of the user submitting the request
   * @return Created result
   */
  @RequestMapping(method = RequestMethod.POST, path = "/create")
  @Secured({"ROLE_PATIENT"})
  @ResponseStatus(HttpStatus.OK)
  public Result createResult(
      @RequestBody Result result,
      Principal principal
  ) {
    log.info("Creating result for user [{}]", principal.getName());
    return resultService.create(result, principal);
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

}
