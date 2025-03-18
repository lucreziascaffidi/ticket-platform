package com.bept4.ticketplatform.repository;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByTitleContaining(String title);

    List<Ticket> findByStatus(com.bept4.ticketplatform.model.Status status);

    List<Ticket> findByOperator(Operator operator);
}
