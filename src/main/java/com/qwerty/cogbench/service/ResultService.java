package com.qwerty.cogbench.service;

import com.qwerty.cogbench.dto.ResultDistriDto;
import com.qwerty.cogbench.model.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface ResultService {

  Result create(Result result, Principal principal);

  Result update(Integer resultId, Result result, Principal principal);

  Result getLatestResult(Principal principal);

  boolean delete(Integer resultId, Principal principal);

  Page<Result> getHistory(Pageable pageable, Principal principal);

  Page<Result> fetchAll(Pageable pageable);

  ResultDistriDto getAccuracyGraphData(Integer bins, Integer nodeNum);

  ResultDistriDto getTimeGraphData(Integer bins, Integer nodeNum);

}
