package com.bept4.ticketplatform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bept4.ticketplatform.exception.ResourceNotFoundException;
import com.bept4.ticketplatform.model.Category;
import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Status;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.repository.TicketRepository;

import jakarta.transaction.Transactional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    // Crea un nuovo ticket
    @Transactional
    public Ticket createTicket(Ticket ticket) {
        ticket.setCreationDate(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    // Ottieni tutti i ticket
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Metodo che recupera i ticket con filtri
    public List<Ticket> getTicketsByFilters(String title, Integer categoryId, String status) {
        boolean hasTitle = title != null && !title.trim().isEmpty();
        boolean hasCategory = categoryId != null;
        boolean hasStatus = status != null && !status.trim().isEmpty();

        if (hasTitle && hasCategory && hasStatus) {
            return ticketRepository.findByTitleContainingIgnoreCaseAndCategoryIdAndStatus(title, categoryId,
                    Status.valueOf(status));
        } else if (hasTitle && hasCategory) {
            return ticketRepository.findByTitleContainingIgnoreCaseAndCategoryId(title, categoryId);
        } else if (hasTitle && hasStatus) {
            return ticketRepository.findByTitleContainingIgnoreCaseAndStatus(title, Status.valueOf(status));
        } else if (hasCategory && hasStatus) {
            return ticketRepository.findByCategoryIdAndStatus(categoryId, Status.valueOf(status));
        } else if (hasTitle) {
            return ticketRepository.findByTitleContainingIgnoreCase(title);
        } else if (hasCategory) {
            return ticketRepository.findByCategoryId(categoryId);
        } else if (hasStatus) {
            return ticketRepository.findByStatus(Status.valueOf(status));
        } else {
            return ticketRepository.findAll(); // Nessun filtro attivo
        }
    }

    // Cerca ticket per titolo
    public List<Ticket> getTicketsByTitle(String title) {
        return ticketRepository.findByTitleContaining(title);
    }

    // Filtra i ticket per stato
    public List<Ticket> getTicketsByStatus(com.bept4.ticketplatform.model.Status status) {
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

    public List<Ticket> getTicketsByCategory(Category category) {
        return ticketRepository.findByCategory(category);
    }

    // Modifica un ticket
    @Transactional
    public Ticket updateTicket(Ticket ticket) {
        ticket.setLastModifiedDate(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    // Elimina ticket
    @Transactional
    public void deleteTicket(Integer id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        if (ticket.isEmpty()) {
            throw new ResourceNotFoundException("Ticket with ID " + id + " not found");
        }
        ticketRepository.deleteById(id);
    }

    public long countAllTickets() {
        return ticketRepository.count();
    }

    public long countByStatus(Status status) {
        return ticketRepository.countByStatus(status);
    }

    public long countByOperator(Operator operator) {
        return ticketRepository.countByOperator(operator);
    }

    public long countByOperatorAndStatus(Operator operator, Status status) {
        return ticketRepository.countByOperatorAndStatus(operator, status);
    }

}