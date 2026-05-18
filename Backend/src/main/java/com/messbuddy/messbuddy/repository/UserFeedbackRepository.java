package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.UserFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFeedbackRepository extends MongoRepository<UserFeedback,String> {
}
