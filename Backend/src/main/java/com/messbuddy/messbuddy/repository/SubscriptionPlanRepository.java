package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.SubscriptionPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPlanRepository extends MongoRepository<SubscriptionPlan,String> {
    List<SubscriptionPlan> findByMessIdAndIsActive(String messId, Boolean isActive);
}
