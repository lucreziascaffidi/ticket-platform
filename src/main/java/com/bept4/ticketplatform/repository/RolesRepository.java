package com.bept4.ticketplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bept4.ticketplatform.model.Roles;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer> {
    Optional<Roles> findByName(String name); // Trova il ruolo per nome
}
