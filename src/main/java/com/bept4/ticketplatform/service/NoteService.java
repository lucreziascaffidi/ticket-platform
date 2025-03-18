package com.bept4.ticketplatform.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bept4.ticketplatform.model.Note;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.repository.NoteRepository;

import jakarta.transaction.Transactional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    // Aggiungi una nuova nota a un ticket
    @Transactional
    public Note addNoteToTicket(Note note) {
        return noteRepository.save(note);
    }

    // Ottieni tutte le note per un ticket
    public List<Note> getNotesByTicket(Ticket ticket) {
        return noteRepository.findByTicket(ticket);
    }
}
