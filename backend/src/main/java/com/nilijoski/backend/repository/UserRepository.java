package com.nilijoski.backend.repository;

import com.nilijoski.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByAccountNumber(String accountNumber);

    Optional<User> findByIban(String iban);

    boolean existsByUsername(String username);
}
