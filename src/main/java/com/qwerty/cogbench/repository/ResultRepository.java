package com.qwerty.cogbench.repository;

import com.qwerty.cogbench.model.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResultRepository extends PagingAndSortingRepository<Result, Integer> {

    Optional<Result> findResultByUserEmail(String email);

    Optional<Result> findResultById(Integer id);

    Optional<Page<Result>> findAllResultByUserEmail(String email, Pageable pageable);

}
