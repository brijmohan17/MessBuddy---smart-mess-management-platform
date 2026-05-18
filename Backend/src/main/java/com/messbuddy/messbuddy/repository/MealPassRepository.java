package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.MealPass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPassRepository extends MongoRepository<MealPass,String> {
    MealPass findByQrCode(String qrCode);
    java.util.Optional<MealPass> findBySubscriptionId(String subscriptionId);
    List<MealPass> findByUserIdAndIsActiveTrueAndValidTillAfter(String userId, java.time.LocalDateTime now);
}
