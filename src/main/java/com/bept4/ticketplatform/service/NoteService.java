package com.bept4.ticketplatform.service;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public Note addNoteToTicket(Note note) {
        return noteRepository.save(note);
    }

    public List<Note> getNotesByTicket(Ticket ticket) {
        return noteRepository.findAll().stream()
                .filter(n -> n.getTicket().getId().equals(ticket.getId()))
                .toList();
    }

    public void saveNote(Note note) {
        noteRepository.save(note);
    }

    public Optional<Note> getNoteById(Integer id) {
        return noteRepository.findById(id);
    }

    public void deleteNote(Integer id) {
        noteRepository.deleteById(id);
    }
}
