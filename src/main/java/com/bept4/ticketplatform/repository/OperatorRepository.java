package com.bept4.ticketplatform.repository;

import com.bept4.ticketplatform.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Integer> {
    Optional<Operator> findByUsername(String username);

    Optional<Operator> findByEmail(String email);
}
