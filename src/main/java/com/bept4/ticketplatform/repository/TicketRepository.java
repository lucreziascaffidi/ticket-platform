package com.bept4.ticketplatform.repository;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Ticket;

import ch.qos.logback.core.status.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByTitleContaining(String title);

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByOperator(Operator operator);
}
