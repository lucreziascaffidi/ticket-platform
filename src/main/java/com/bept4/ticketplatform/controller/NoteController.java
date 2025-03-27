package com.bept4.ticketplatform.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bept4.ticketplatform.model.Note;
import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.service.NoteService;
import com.bept4.ticketplatform.service.TicketService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tickets")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private TicketService ticketService;

    // Aggiunge nota
    @PostMapping("/{ticketId}/note")
    public String addNote(
            @PathVariable Integer ticketId,
            @Valid @ModelAttribute("note") Note note,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        Operator loggedOperator = (Operator) model.getAttribute("loggedOperator");
        if (loggedOperator == null)
            return "redirect:/login";

        Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);
        if (ticketOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Ticket not found.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            return "redirect:/tickets";
        }

        Ticket ticket = ticketOpt.get();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "Validation failed for your note.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            return "redirect:/tickets/show/" + ticketId;
        }

        note.setTicket(ticket);
        note.setCreationDate(LocalDateTime.now());
        note.setAuthor(loggedOperator);
        noteService.saveNote(note);

        ticket.setLastModifiedDate(LocalDateTime.now());
        ticketService.updateTicket(ticket);

        redirectAttributes.addFlashAttribute("message", "Note added successfully.");
        redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        return "redirect:/tickets/show/" + ticketId;
    }

    // Cancella nota del ticket
    @PostMapping("/notes/delete/{id}")
    public String deleteNote(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Note> noteOpt = noteService.getNoteById(id);
            if (noteOpt.isPresent()) {
                Integer ticketId = noteOpt.get().getTicket().getId();
                noteService.deleteNote(id);
                redirectAttributes.addFlashAttribute("message", "Note deleted successfully.");
                redirectAttributes.addFlashAttribute("messageClass", "alert-success");
                return "redirect:/tickets/show/" + ticketId;
            } else {
                redirectAttributes.addFlashAttribute("message", "Note not found.");
                redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "An error occurred while deleting the note.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/tickets";
    }
}
