package com.qwerty.cogbench.service;

import com.qwerty.cogbench.dto.ResultDistriDto;
import com.qwerty.cogbench.model.Result;

import java.security.Principal;
import java.util.List;

public interface ResultService {

  Result create(Result result, Principal principal);

  Result update(Integer resultId, Result result, Principal principal);

  Result fetch(Integer resultId, Principal principal);

  Result getLatestResult(Principal principal);

  boolean delete(Integer resultId, Principal principal);

  List<Result> fetchResultsWithUserEmail(String userEmail);

  List<Result> getHistory(Principal principal);

  List<Result> fetchAll();

  ResultDistriDto getAccuracyGraphData(Integer bins, Integer nodeNum);

  ResultDistriDto getTimeGraphData(Integer bins, Integer nodeNum);

}
