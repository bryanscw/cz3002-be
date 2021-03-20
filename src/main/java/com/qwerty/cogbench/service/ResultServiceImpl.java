package com.qwerty.cogbench.service;

import com.qwerty.cogbench.exception.ResourceNotFoundException;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.ResultRepository;
import com.qwerty.cogbench.repository.UserRepository;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResultServiceImpl implements ResultService {

  private final ResultRepository resultRepository;

  private final UserRepository userRepository;

  public ResultServiceImpl(ResultRepository resultRepository, UserRepository userRepository) {
    this.resultRepository = resultRepository;
    this.userRepository = userRepository;
  }

  @Override
  public Page<Result> fetchAll(Pageable pageable) {
    return resultRepository.findAll(pageable);
  }

  @Override
  public Result create(Result result, Principal principal) {

    User userToFind = userRepository.findUserByEmail(principal.getName()).orElseThrow(() -> {
      String errorMsg = String.format("User with email [%s] not found", principal.getName());
      log.error(errorMsg);
      throw new ResourceNotFoundException(errorMsg);
    });

    result.setUser(userToFind);

    return resultRepository.save(result);
  }

  @Override
  public Result getLatestResult(Principal principal) {

    return resultRepository
        .findFirstByUserEmailOrderByLastModifiedDateDesc(principal.getName())
        .orElseThrow(
            () -> new ResourceNotFoundException(
                String.format("User with email [%s] not found", principal.getName())));
  }

  public Page<Result> getHistory(Pageable pageable, Principal principal) {

    return resultRepository.findAllResultByUserEmail(principal.getName(), pageable).orElseThrow(
        () -> new ResourceNotFoundException(
            String.format("Results for user [%s] not found", principal.getName())));
  }

  @Override
  public boolean delete(Integer resultId, Principal principal) {

    Result resultToFind = resultRepository
        .findResultById(resultId)
        .orElseThrow(() -> {
          String progressErrorMsg = String
              .format("Result for User with userEmail: [%s] and resultId: [%s] not found",
                  principal.getName(), resultId);
          log.error(progressErrorMsg);
          throw new ResourceNotFoundException(progressErrorMsg);
        });

    // Delete the Progress
    resultRepository.deleteById(resultToFind.getId());

    return true;
  }
}
