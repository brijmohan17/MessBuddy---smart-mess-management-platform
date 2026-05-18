package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.UserSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriptionRepository extends MongoRepository<UserSubscription,String> {
    List<UserSubscription> findByUserId(String userId);
}
