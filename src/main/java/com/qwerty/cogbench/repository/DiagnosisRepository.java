package com.qwerty.cogbench.repository;

import com.qwerty.cogbench.model.Diagnosis;
import com.qwerty.cogbench.model.Result;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosisRepository extends PagingAndSortingRepository<Diagnosis, Integer> {

    Optional<Diagnosis> findDiagnosisById(Integer id);

    Optional<Diagnosis> findDiagnosisByResult(Result result);

}
