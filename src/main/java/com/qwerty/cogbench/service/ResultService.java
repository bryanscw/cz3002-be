package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface ResultService {

    Result create(String userEmail, Result result, Authentication authentication);

    Result getLatestResult(String email, Authentication authentication);

    boolean delete(String userEmail, Integer resultId, Authentication authentication);

    Page<Result> getHistory(String email, Pageable pageable, Authentication authentication);

    Page<Result> fetchAll(Pageable pageable);

}
