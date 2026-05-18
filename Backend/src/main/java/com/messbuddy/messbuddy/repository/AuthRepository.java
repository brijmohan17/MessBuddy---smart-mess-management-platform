package com.messbuddy.messbuddy.repository;

import com.messbuddy.messbuddy.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends MongoRepository<User,String> {
	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	Optional<User> findByUsernameAndLoginRole(String username, com.messbuddy.messbuddy.entity.type.LoginRole loginRole);
}
