package com.bept4.ticketplatform.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bept4.ticketplatform.exception.ResourceNotFoundException;
import com.bept4.ticketplatform.exception.ValidationException;
import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.service.OperatorService;
import com.bept4.ticketplatform.service.TicketService;
import com.bept4.ticketplatform.model.Status;

import jakarta.validation.Valid;

@RestController
@RequestMapping("tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private OperatorService operatorService;

    // Crea un nuovo ticket
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket) {
        try {
            Ticket createdTicket = ticketService.createTicket(ticket);
            return ResponseEntity.ok(createdTicket);
        } catch (Exception e) {
            throw new ValidationException("Errore nella creazione del ticket");
        }
    }

    // Ottieni tutti i ticket
    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // Cerca ticket per titolo
    @GetMapping("/search")
    public List<Ticket> getTicketsByTitle(@RequestParam String title) {
        List<Ticket> tickets = ticketService.getTicketsByTitle(title);
        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("Nessun ticket trovato con il titolo " + title);
        }
        return tickets;
    }

    // Filtra ticket per stato
    @GetMapping("/status")
    public List<Ticket> getTicketsByStatus(@RequestParam String status) {
        List<Ticket> tickets = ticketService.getTicketsByStatus(Status.valueOf(status.toUpperCase()));
        if (tickets.isEmpty()) {
            throw new ResourceNotFoundException("Nessun ticket trovato con lo stato " + status);
        }
        return tickets;
    }

    // Filtra ticket per operatore
    @GetMapping("/operator")
    public List<Ticket> getTicketsByOperator(@RequestParam Integer operatorId) {
        Optional<Operator> operator = operatorService.getOperatorById(operatorId);
        if (operator.isEmpty()) {
            throw new ResourceNotFoundException("Operatore con ID " + operatorId + " non trovato");
        }
        return ticketService.getTicketsByOperator(operator.get());
    }

    // Trova ticket per ID
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Integer id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        if (ticket.isEmpty()) {
            throw new ResourceNotFoundException("Ticket con ID " + id + " non trovato");
        }
        return ResponseEntity.ok(ticket.get());
    }

    // Aggiorna un ticket
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Integer id, @Valid @RequestBody Ticket ticket) {
        ticket.setId(id);
        Ticket updatedTicket = ticketService.updateTicket(ticket);
        if (updatedTicket == null) {
            throw new ResourceNotFoundException("Ticket con ID " + id + " non trovato per l'aggiornamento");
        }
        return ResponseEntity.ok(updatedTicket);
    }

    // Elimina un ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Integer id) {
        ticketService.deleteTicket(id); // Non assegnare il risultato a una variabile
        return ResponseEntity.noContent().build();
    }
}
