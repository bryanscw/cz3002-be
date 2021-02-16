package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface ResultService {

  Result create(Result result, Principal principal);

  Result getLatestResult(Principal principal);

  boolean delete(Integer resultId, Principal principal);

  Page<Result> getHistory(Pageable pageable, Principal principal);

  Page<Result> fetchAll(Pageable pageable);

}
