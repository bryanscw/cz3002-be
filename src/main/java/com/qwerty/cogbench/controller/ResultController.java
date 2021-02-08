package com.qwerty.cogbench.controller;

import com.qwerty.cogbench.exception.ResourceNotFoundException;
import com.qwerty.cogbench.exception.UnauthorizedException;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.service.ResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping(
        value = {"/result"},
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService){ this.resultService = resultService; }

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
     * @param userEmail Email of user owning the result
     * @param authentication Authentication context containing information of the user submitting the request
     * @return Result
     */
    @RequestMapping(method = RequestMethod.POST, path = "/{userEmail}/latest")
    @Secured({})
    @ResponseStatus(HttpStatus.OK)
    public Result getLatestUserResult(
            @PathVariable(value = "userEmail") String userEmail,
            Authentication authentication) {
        log.info("Getting latest result for user [{}]", userEmail);
        return resultService.getLatestResult(userEmail, authentication);
    }

    /**
     * Get result.
     *
     * @param userEmail Email of user owning the result
     * @param pageable Pagination context
     * @param authentication Authentication context containing information of the user submitting the request
     * @return Result
     */
    @RequestMapping(method = RequestMethod.POST, path = "/{userEmail}/all")
    @Secured({"ROLE_CANDIDATE"})
    @ResponseStatus(HttpStatus.OK)
    public Page<Result> fetchAllUserResult(
            @PathVariable(value = "userEmail") String userEmail,
            Pageable pageable,
            Authentication authentication) {
        log.info("Getting all result for user [{}]", userEmail);
        return resultService.getHistory(userEmail, pageable, authentication);
    }

    /**
     * Create a new result.
     *
     * @param userEmail Email of user owning the result
     * @param result Result to be created
     * @param authentication Authentication context containing information of the user submitting the request
     * @return Created result
     */
    @RequestMapping(method = RequestMethod.POST, path = "/{userEmail}/create")
    @Secured({"ROLE_CANDIDATE"})
    @ResponseStatus(HttpStatus.OK)
    public Result createResult(
            @PathVariable(value = "userEmail") String userEmail,
            Result result,
            Authentication authentication) {
        log.info("Creating result for user [{}]", result.getUser());
        return resultService.create(userEmail, result, authentication);
    }

    /**
     * Delete a Result.
     * <p>
     *
     * @param userEmail Email of user owning the result
     * @param resultId Result id of result to be deleted
     * @param authentication Authentication context containing information of the user submitting the request
     * @return Flag indicating if request is successful
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/{userEmail}/delete/{resultId}")
    @ResponseStatus(HttpStatus.OK)
    @Secured({"ROLE_CANDIDATE"})
    public boolean deleteUser(
            @PathVariable(value = "userEmail") String userEmail,
            @PathVariable(value = "resultId") Integer resultId,
            Authentication authentication) {
        log.info("Deleting result [{}] for user [{}]", resultId, userEmail);
        return resultService.delete(userEmail, resultId, authentication);
    }

}
