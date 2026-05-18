package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.CheckIn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends MongoRepository<CheckIn,String> {
    List<CheckIn> findByMessIdAndCreatedAtBetweenAndStatus(String messId, java.time.LocalDateTime start, java.time.LocalDateTime end, String status);
}
