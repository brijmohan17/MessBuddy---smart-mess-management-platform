package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.ForumPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForumPostRepository extends MongoRepository<ForumPost,String> {
}
