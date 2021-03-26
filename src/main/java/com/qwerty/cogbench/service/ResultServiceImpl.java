package com.qwerty.cogbench.service;

import com.qwerty.cogbench.dto.ResultDistriDto;
import com.qwerty.cogbench.exception.ForbiddenException;
import com.qwerty.cogbench.exception.ResourceNotFoundException;
import com.qwerty.cogbench.exception.UnauthorizedException;
import com.qwerty.cogbench.model.Result;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.ResultRepository;
import com.qwerty.cogbench.repository.UserRepository;
import com.qwerty.cogbench.util.Histogram;
import java.security.Principal;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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

  private static ResultDistriDto buildResultDtriDto(List<Double> doubleList, Integer bins) {
    final DoubleSummaryStatistics statistics = doubleList.stream().mapToDouble(x -> x)
        .summaryStatistics();
    final double max = statistics.getMax();
    final double min = statistics.getMin();
    final double binSize = (max - min) / bins;
    final Histogram histogram = Histogram.from(doubleList);
    final Map<Integer, Integer> frequencies = histogram.histogram(bins);

    List<Integer> data = new ArrayList<>();
    frequencies.forEach((key, value) -> data.add(value));

    List<String> labels = new ArrayList<>();
    for (int i = 0; i < bins; i++) {
      labels.add(String.format("[%.2f,%.2f)", min + i * binSize, min + (i + 1) * binSize));
    }

    return new ResultDistriDto(labels, data);
  }

  @Override
  public List<Result> fetchAll() {
    return resultRepository.findAll();
  }

  @Override
  public List<Result> fetchResultsWithUserEmail(String userEmail){
    return resultRepository.findResultByUserEmail(userEmail)
            .orElseThrow(() -> {
              String errorMsg = String.format("User with email [%s] not found", userEmail);
              log.error(errorMsg);
              throw new ResourceNotFoundException(errorMsg);
            });
  }

  @Override
  public Result create(Result result, Principal principal) {

    if (result.getUser().getEmail().equals(null)) {
      String errorMsg = "User of result is empty";
      log.error(errorMsg);
      throw new ResourceNotFoundException(errorMsg);
    }

    User userToFind = userRepository.findUserByEmail(result.getUser().getEmail())
        .orElseThrow(() -> {
          String errorMsg = String.format("User with email [%s] not found", result.getUser().getEmail());
          log.error(errorMsg);
          throw new ResourceNotFoundException(errorMsg);
        });

    result.setUser(userToFind);

    return resultRepository.save(result);
  }

  @Override
  public Result fetch(Integer resultId) {
    return resultRepository.findResultById(resultId)
            .orElseThrow(() -> {
              String errorMsg = String.format("Result with Id [%s] not found", resultId);
              log.error(errorMsg);
              throw new ResourceNotFoundException(errorMsg);
            });
  }

  @Override
  public Result update(Integer resultId, Result result, Principal principal) {

    User userToFind = userRepository.findUserByEmail(principal.getName()).orElseThrow(() -> {
      String errorMsg = String.format("User with email [%s] not found", principal.getName());
      log.error(errorMsg);
      throw new ResourceNotFoundException(errorMsg);
    });

    Result resultToFind = resultRepository.findResultById(resultId).orElseThrow(() -> {
      String errorMsg = String.format("Result with Id [%s] not found", resultId);
      log.error(errorMsg);
      throw new ResourceNotFoundException(errorMsg);
    });

    if (resultToFind.getAccuracy() != null && resultToFind.getTime() != null) {
      String errorMsg = String.format("Result with Id [%s] cannot be updated", resultId);
      log.error(errorMsg);
      throw new ForbiddenException(errorMsg);
    }

    if (userToFind.getRole().equals("ROLE_PATIENT") && !resultToFind.getUser().getEmail()
        .equals(userToFind.getEmail())) {
      String progressErrorMsg = String
          .format("User with Id [%s] not authorized to update result for user with Id [%s]",
              resultToFind.getUser().getName(),
              userToFind.getName());
      log.error(progressErrorMsg);
      throw new UnauthorizedException(progressErrorMsg);
    }

    if (userToFind.getRole().equals("ROLE_DOCTOR")) {
      resultToFind.setNodeNum(result.getNodeNum());
    } else {
      resultToFind.setAccuracy(result.getAccuracy());
      resultToFind.setTime(result.getTime());
    }

    return resultRepository.save(resultToFind);
  }

  @Override
  public Result getLatestResult(Principal principal) {

    return resultRepository
        .findFirstByUserEmailOrderByLastModifiedDateDesc(principal.getName())
        .orElseThrow(
            () -> new ResourceNotFoundException(
                String.format("User with email [%s] not found", principal.getName())));
  }

  public List<Result> getHistory(Principal principal) {

    return resultRepository.findResultByUserEmail(principal.getName()).orElseThrow(
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

  @Override
  public ResultDistriDto getAccuracyGraphData(Integer bins, Integer nodeNum) {
    List<Double> accuracies = resultRepository.findAccuracyByNodeNum(nodeNum);
    return buildResultDtriDto(accuracies, bins);
  }

  @Override
  public ResultDistriDto getTimeGraphData(Integer bins, Integer nodeNum) {
    List<Double> times = resultRepository.findTimeByNodeNum(nodeNum);
    return buildResultDtriDto(times, bins);
  }
}
