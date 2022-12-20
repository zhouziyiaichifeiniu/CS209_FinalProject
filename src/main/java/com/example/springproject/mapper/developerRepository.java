package com.example.springproject.mapper;

import com.example.springproject.domain.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface developerRepository extends JpaRepository<Developer,Long> {
  @Query(nativeQuery=true, value ="select p from Developer p order by p.commit_num desc limit 3")
  List<Developer> findTopThreeDeveloperByCommit();
}
