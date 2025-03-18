package com.bept4.ticketplatform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bept4.ticketplatform.exception.ResourceNotFoundException;
import com.bept4.ticketplatform.model.Note;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.service.NoteService;
import com.bept4.ticketplatform.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/notes")
public class NotesController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private TicketService ticketService;

    // Aggiungi una nuova nota a un ticket
    @PostMapping
    public ResponseEntity<Note> addNoteToTicket(@Valid @RequestBody Note note) {
        Ticket ticket = ticketService.getTicketById(note.getTicket().getId()).orElseThrow(
                () -> new ResourceNotFoundException("Ticket con ID " + note.getTicket().getId() + " non trovato"));
        note.setTicket(ticket);
        Note createdNote = noteService.addNoteToTicket(note);
        return ResponseEntity.ok(createdNote);
    }

    // Ottieni tutte le note per un ticket
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<Note>> getNotesByTicket(@PathVariable Integer ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket con ID " + ticketId + " non trovato"));
        List<Note> notes = noteService.getNotesByTicket(ticket);
        return ResponseEntity.ok(notes);
    }

}
