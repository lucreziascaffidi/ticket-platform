package com.bept4.ticketplatform.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bept4.ticketplatform.exception.ResourceNotFoundException;
import com.bept4.ticketplatform.model.Note;
import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.service.CategoryService;
import com.bept4.ticketplatform.service.OperatorService;
import com.bept4.ticketplatform.service.TicketService;
import com.bept4.ticketplatform.model.Status;

import jakarta.validation.Valid;

@Controller
@RequestMapping("tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private CategoryService categoryService;

    private Operator getLoggedOperator(Model model) {
        return (Operator) model.getAttribute("loggedOperator");
    }

    private boolean isAdmin(Model model) {
        Boolean adminAttr = (Boolean) model.getAttribute("isAdmin");
        return Boolean.TRUE.equals(adminAttr);
    }

    @GetMapping
    public String getTicketsPage(Model model) {
        Operator loggedOperator = getLoggedOperator(model);
        boolean isAdmin = isAdmin(model);

        model.addAttribute("statuses", Status.values());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("current", "Tickets");

        if (isAdmin) {
            model.addAttribute("tickets", ticketService.getAllTickets());
        } else if (loggedOperator != null) {
            model.addAttribute("tickets", ticketService.getTicketsByOperator(loggedOperator));
        } else {
            model.addAttribute("tickets", List.of());
        }

        return "tickets/index";
    }

    @GetMapping("/search")
    public String getTicketsByFilters(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            Model model) {

        Operator loggedOperator = getLoggedOperator(model);
        boolean isAdmin = isAdmin(model);

        List<Ticket> tickets = ticketService.getTicketsByFilters(title, categoryId, status);

        if (!isAdmin && loggedOperator != null) {
            tickets = tickets.stream()
                    .filter(ticket -> ticket.getOperator() != null &&
                            ticket.getOperator().getId().equals(loggedOperator.getId()))
                    .toList();
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("title", title);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("current", "Tickets");

        if (tickets.isEmpty()) {
            model.addAttribute("message", "No tickets found.");
            model.addAttribute("messageClass", "alert-danger");
        }

        return "tickets/index";
    }

    @GetMapping("/show/{id}")
    public String show(@PathVariable Integer id, Model model) {
        Operator loggedOperator = getLoggedOperator(model);
        boolean isAdmin = isAdmin(model);

        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        if (ticketOpt.isEmpty()) {
            model.addAttribute("message", "Ticket not found.");
            model.addAttribute("messageClass", "alert-danger");
            return "redirect:/tickets";
        }

        Ticket ticket = ticketOpt.get();
        model.addAttribute("ticket", ticket);
        model.addAttribute("note", new Note());

        if (!isAdmin &&
                (ticket.getOperator() == null ||
                        loggedOperator == null ||
                        !ticket.getOperator().getId().equals(loggedOperator.getId()))) {

            model.addAttribute("message", "You are not authorized to view this ticket.");
            model.addAttribute("messageClass", "alert-danger");
            return "redirect:/tickets";
        }

        return "tickets/show";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("create", true);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("operators", operatorService.getAllOperators());
        return "tickets/create-or-edit";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("ticket") Ticket ticket,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        ticket.setStatus(Status.TO_DO);

        if (bindingResult.hasErrors()) {
            model.addAttribute("create", true);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("operators", operatorService.getAllOperators());
            return "tickets/create-or-edit";
        }

        try {
            ticketService.createTicket(ticket);
            redirectAttributes.addFlashAttribute("message", "A new ticket has been created successfully!");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "An error occurred while creating the ticket.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/tickets";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        if (!isAdmin(model)) {
            return "redirect:/tickets";
        }

        try {
            Ticket ticket = ticketService.getTicketById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
            model.addAttribute("create", false);
            model.addAttribute("ticket", ticket);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("operators", operatorService.getAllOperators());
        } catch (ResourceNotFoundException e) {
            model.addAttribute("message", "Ticket not found.");
            model.addAttribute("messageClass", "alert-danger");
            return "redirect:/tickets";
        }

        return "tickets/create-or-edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id,
            @Valid Ticket ticket,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("operators", operatorService.getAllOperators());
            return "tickets/create-or-edit";
        }

        try {
            Ticket existing = ticketService.getTicketById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

            ticket.setId(id);
            ticket.setCreationDate(existing.getCreationDate());
            ticketService.updateTicket(ticket);

            redirectAttributes.addFlashAttribute("message", "Ticket updated successfully!");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "An error occurred while updating the ticket.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/tickets";
    }

    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Integer id,
            @RequestParam("status") Status status,
            Model model,
            RedirectAttributes redirectAttributes) {

        Operator loggedOperator = getLoggedOperator(model);

        Optional<Ticket> optionalTicket = ticketService.getTicketById(id);
        if (optionalTicket.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Ticket not found.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            return "redirect:/tickets";
        }

        Ticket ticket = optionalTicket.get();

        if (ticket.getOperator() != null &&
                loggedOperator != null &&
                ticket.getOperator().getId().equals(loggedOperator.getId())) {

            ticket.setStatus(status);
            ticket.setLastModifiedDate(LocalDateTime.now());
            ticketService.updateTicket(ticket);

            redirectAttributes.addFlashAttribute("message", "Status updated successfully.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } else {
            redirectAttributes.addFlashAttribute("message", "You are not authorized to update this ticket.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/tickets/show/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteTicket(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("message", "Ticket deleted successfully.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "Ticket not found.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/tickets";
    }
}
