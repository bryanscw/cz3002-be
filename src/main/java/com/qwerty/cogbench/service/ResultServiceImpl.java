package com.qwerty.cogbench.service;

import com.qwerty.cogbench.exception.ResourceNotFoundException;
import com.qwerty.cogbench.exception.UnauthorizedException;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.ResultRepository;
import com.qwerty.cogbench.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;

    private final UserRepository userRepository;

    public ResultServiceImpl(ResultRepository resultRepository, UserRepository userRepository){
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<Result> fetchAll(Pageable pageable) {
        return resultRepository.findAll(pageable);
    }

    @Override
    public Result create(String userEmail, Result result, Authentication authentication) {
        // Check if user is authorized to perform action
        this.isAuthorized(userEmail, authentication);

        // Find the referenced User and GameMap
        User userToFind = userRepository.findUserByEmail(userEmail).orElseThrow(() -> {
            String errorMsg = String.format("User with email [%s] not found", userEmail);
            log.error(errorMsg);
            return new ResourceNotFoundException(errorMsg);
        });

        result.setUser(userToFind);

        return resultRepository.save(result);
    }

    @Override
    public Result getLatestResult(String userEmail, Authentication authentication) {
        // Check if user is authorized to perform action
        this.isAuthorized(userEmail, authentication);

        return resultRepository.findResultByUserEmail(userEmail).orElseThrow(
                () -> new ResourceNotFoundException(
                        String.format("User with email [%s] not found", userEmail)));
    }

    public Page<Result> getHistory(String userEmail, Pageable pageable, Authentication authentication) {
        // Check if user is authorized to perform action
        this.isAuthorized(userEmail, authentication);

        return resultRepository.findAllResultByUserEmail(userEmail, pageable).orElseThrow(
                () -> new ResourceNotFoundException(
                        String.format("Results for user [%s] not found", userEmail)));
    }

    @Override
    public boolean delete(String userEmail, Integer resultId, Authentication authentication) {
        // Check if user is authorized to perform action
        this.isAuthorized(userEmail, authentication);

        Result resultToFind = resultRepository
                .findResultById(resultId)
                .orElseThrow(() -> {
                    String progressErrorMsg = String
                            .format("Result for User with userEmail: [%s] and resultId: [%s] not found",
                                    userEmail, resultId);
                    log.error(progressErrorMsg);
                    throw new ResourceNotFoundException(progressErrorMsg);
                });

        // Delete the Progress
        resultRepository.deleteById(resultToFind.getId());

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
    private void isAuthorized(String userEmail, Authentication authentication) {
        String principalName = ((org.springframework.security.core.userdetails.User) authentication
                .getPrincipal()).getUsername();

        // User is only allowed to submit answers for themselves
        if (!userEmail.equals(principalName)) {
            String errorMsg = String.format(
                    "User with userEmail: [%s] is not allowed to submit answers for user with userEmail: [%s]",
                    principalName, userEmail);
            throw new UnauthorizedException(errorMsg);
        }

    }
}
