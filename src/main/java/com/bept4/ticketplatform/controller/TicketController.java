package com.bept4.ticketplatform.controller;

import java.util.Collections;
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
        Ticket createdTicket = ticketService.createTicket(ticket);
        return ResponseEntity.ok(createdTicket);
    }

    // Ottieni tutti i ticket
    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // Cerca ticket per titolo
    @GetMapping("/search")
    public List<Ticket> getTicketsByTitle(@RequestParam String title) {
        return ticketService.getTicketsByTitle(title);
    }

    // Filtra ticket per stato
    @GetMapping("/status")
    public List<Ticket> getTicketsByStatus(@RequestParam String status) {
        return ticketService.getTicketsByStatus(Status.valueOf(status.toUpperCase())); // Converte la stringa in Status
    }

    // Filtra ticket per operatore
    @GetMapping("/operator")
    public List<Ticket> getTicketsByOperator(@RequestParam Integer operatorId) {
        // Supponiamo che tu abbia un metodo per ottenere l'operatore per ID
        Optional<Operator> operator = operatorService.getOperatorById(operatorId);
        return operator.map(ticketService::getTicketsByOperator).orElseGet(Collections::emptyList);
    }

    // Trova ticket per ID
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Integer id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Aggiorna un ticket
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Integer id, @Valid @RequestBody Ticket ticket) {
        ticket.setId(id);
        Ticket updatedTicket = ticketService.updateTicket(ticket);
        return ResponseEntity.ok(updatedTicket);
    }

    // Elimina un ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Integer id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
