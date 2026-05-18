package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.Prebooking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrebookingRepository extends MongoRepository<Prebooking,String> {
    List<Prebooking> findByUserId(String userId);
    List<Prebooking> findByMessId(String messId);
}
