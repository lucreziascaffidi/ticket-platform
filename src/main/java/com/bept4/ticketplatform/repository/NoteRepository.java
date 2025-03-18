package com.bept4.ticketplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bept4.ticketplatform.model.Note;
import com.bept4.ticketplatform.model.Ticket;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Integer> {
    List<Note> findByTicket(Ticket ticket); // Trova tutte le note associate a un ticket
}
