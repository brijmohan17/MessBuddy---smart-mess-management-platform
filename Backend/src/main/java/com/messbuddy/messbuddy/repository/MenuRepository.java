package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.Menu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends MongoRepository<Menu,String> {
    @Query(value = "{ 'Owner_ID': ?0 }")
    List<Menu> findByOwnerId(String ownerId);
}
