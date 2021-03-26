package com.qwerty.cogbench.repository;

import com.qwerty.cogbench.model.Result;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultRepository extends JpaRepository<Result, Integer> {

  Optional<Result> findFirstByUserEmailOrderByLastModifiedDateDesc(String email);

  Optional<Result> findResultById(Integer id);

  Optional<List<Result>> findResultByUserEmail(String email);

  @Query(value = "SELECT r.accuracy FROM result r WHERE r.node_num = ?1 AND r.time IS NOT NULL", nativeQuery = true)
  List<Double> findAccuracyByNodeNum(Integer nodeNum);

  @Query(value = "SELECT r.time FROM result r WHERE r.node_num = ?1 AND r.time IS NOT NULL", nativeQuery = true)
  List<Double> findTimeByNodeNum(Integer nodeNum);

}
