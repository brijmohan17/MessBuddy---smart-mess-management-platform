package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.Mess;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface MessRepository extends MongoRepository<Mess,String> {
    @Query(value = "{ 'Owner_ID': ?0 }")
    Optional<Mess> findByOwnerId(String ownerId);

    @Query(value = "{ 'Owner_ID': ?0 }")
    List<Mess> findAllByOwnerId(String ownerId);
}
