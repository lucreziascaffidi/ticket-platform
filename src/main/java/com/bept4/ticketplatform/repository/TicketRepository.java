package com.bept4.ticketplatform.repository;

import com.bept4.ticketplatform.model.Category;
import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Status;
import com.bept4.ticketplatform.model.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByTitleContaining(String title);

    List<Ticket> findByCategoryId(Integer categoryId);

    List<Ticket> findByOperator(Operator operator);

    List<Ticket> findByCategory(Category category);

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByTitleContainingAndCategoryIdAndStatus(String title, Integer categoryId, Status status);

    List<Ticket> findByTitleContainingAndCategoryId(String title, Integer categoryId);

    List<Ticket> findByTitleContainingAndStatus(String title, Status status);

    List<Ticket> findByCategoryIdAndStatus(Integer categoryId, Status status);

    List<Ticket> findByTitleContainingIgnoreCase(String title);

    List<Ticket> findByTitleContainingIgnoreCaseAndCategoryId(String title, Integer categoryId);

    List<Ticket> findByTitleContainingIgnoreCaseAndStatus(String title, Status status);

    List<Ticket> findByTitleContainingIgnoreCaseAndCategoryIdAndStatus(String title, Integer categoryId, Status status);

    long countByStatus(Status status);

    long countByOperator(Operator operator);

    long countByOperatorAndStatus(Operator operator, Status status);
}
