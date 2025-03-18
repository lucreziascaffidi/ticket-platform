package com.bept4.ticketplatform.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.repository.TicketRepository;

import ch.qos.logback.core.status.Status;
import jakarta.transaction.Transactional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    // Crea un nuovo ticket
    @Transactional
    public Ticket createTicket(Ticket ticket) {
        ticket.setCreationDate(java.time.LocalDateTime.now()); // Imposta la data di creazione
        return ticketRepository.save(ticket);
    }

    // Ottieni tutti i ticket
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Cerca ticket per titolo
    public List<Ticket> getTicketsByTitle(String title) {
        return ticketRepository.findByTitleContaining(title);
    }

    // Filtra i ticket per stato
    public List<Ticket> getTicketsByStatus(Status status) {
        return ticketRepository.findByStatus(status);
    }

    // Filtra i ticket per operatore
    public List<Ticket> getTicketsByOperator(Operator operator) {
        return ticketRepository.findByOperator(operator);
    }

    // Trova un ticket per ID
    public Optional<Ticket> getTicketById(Integer id) {
        return ticketRepository.findById(id);
    }

    // Modifica un ticket
    @Transactional
    public Ticket updateTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    // Elimina un ticket
    @Transactional
    public void deleteTicket(Integer id) {
        ticketRepository.deleteById(id);
    }
}
