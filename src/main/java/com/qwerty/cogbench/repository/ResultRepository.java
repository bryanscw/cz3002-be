package com.qwerty.cogbench.repository;

import com.qwerty.cogbench.model.Result;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResultRepository extends CrudRepository<Result, Integer> {

    Optional<Result> findFirstByUserEmailOrderByLastModifiedDateDesc(String email);

    Optional<Result> findResultById(Integer id);

    Optional<List<Result>> findAllResultByUserEmail(String email, List<Result> list);

    @Query(value="SELECT r.accuracy FROM result r WHERE r.node_num = ?1", nativeQuery= true)
    List<Double> findAllAccuracyByNodeNum(Integer nodeNum);

    @Query(value="SELECT r.time FROM result r WHERE r.node_num = ?1", nativeQuery= true)
    List<Double> findAllTimeByNodeNum(Integer nodeNum);

}
