package com.bept4.ticketplatform.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.bept4.ticketplatform.model.Category;
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

    // Ottieni la pagina dei tickets per l'admin o per l'operatore
    @GetMapping
    public String getTicketsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("statuses", Status.values());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("current", "Tickets");

        if (isAdmin) {
            model.addAttribute("tickets", ticketService.getAllTickets());
        } else {
            if (loggedOperator.isPresent()) {
                model.addAttribute("tickets", ticketService.getTicketsByOperator(loggedOperator.get()));
            } else {
                model.addAttribute("tickets", List.of());
            }
        }

        return "tickets/index";
    }

    // Ottieni tutti i ticket
    @GetMapping("/all")
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // Search
    // Metodo per la ricerca dei ticket
    @GetMapping("/search")
    public String getTicketsByFilters(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails // Aggiunto
    ) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("title", title);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);

        List<Ticket> tickets = ticketService.getTicketsByFilters(title, categoryId, status);

        if (!isAdmin && loggedOperator.isPresent()) {
            tickets = tickets.stream()
                    .filter(ticket -> ticket.getOperator().getId().equals(loggedOperator.get().getId()))
                    .toList();
        }

        if (tickets.isEmpty()) {
            model.addAttribute("message", "No tickets found.");
            model.addAttribute("messageClass", "alert-danger");
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("current", "Tickets");

        return "tickets/index";
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

    // Filtra ticket per categoria
    @GetMapping("/category")
    public List<Ticket> getTicketsByCategory(@RequestParam Integer categoryId) {
        Optional<Category> category = categoryService.getCategoryById(categoryId);
        if (category.isEmpty()) {
            throw new ResourceNotFoundException("Categoria con ID " + categoryId + " non trovata");
        }
        return ticketService.getTicketsByCategory(category.get());
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

    // Pagina di visualizzazione di un ticket per ID
    @GetMapping("/show/{id}")
    public String show(@PathVariable Integer id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

        Optional<Ticket> ticket = ticketService.getTicketById(id);
        if (ticket.isEmpty()) {
            model.addAttribute("message", "Ticket not found.");
            model.addAttribute("messageClass", "alert-danger");
            return "redirect:/tickets"; // Se il ticket non esiste, reindirizza alla lista dei ticket
        }

        Ticket ticketDetails = ticket.get();
        model.addAttribute("ticket", ticketDetails);
        model.addAttribute("note", new Note());

        if (ticketDetails.getOperator() != null) {
            model.addAttribute("operator", ticketDetails.getOperator());
        }

        if (userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("isAdmin", true);
        } else {
            if (ticketDetails.getOperator() == null
                    || !ticketDetails.getOperator().getUsername().equals(userDetails.getUsername())) {
                model.addAttribute("message", "You are not authorized to view this ticket.");
                model.addAttribute("messageClass", "alert-danger");
                return "redirect:/tickets";
            }
        }

        return "tickets/show";
    }

    // Crea un ticket
    @GetMapping("/create")
    public String create(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));
        model.addAttribute("ticket", new Ticket()); // Crea un oggetto ticket vuoto
        model.addAttribute("create", true); // Indica che si tratta di una creazione
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("operators", operatorService.getAllOperators());

        return "tickets/create-or-edit";
    }

    // Salva un ticket nuovo
    @PostMapping("/create")
    public String store(@Valid @ModelAttribute("ticket") Ticket ticket,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

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

    // Modifica ticket
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model, @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:/tickets";
        }

        try {
            Ticket ticket = ticketService.getTicketById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket with ID " + id + " not found"));
            model.addAttribute("create", false);
            model.addAttribute("ticket", ticket);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("operators", operatorService.getAllOperators());
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("message", "Ticket not found.");
            model.addAttribute("messageClass", "alert-danger");
            return "redirect:/tickets";
        }

        return "tickets/create-or-edit";
    }

    // Aggiorna ticket dopo la modifica
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id, @Valid Ticket ticket,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

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

    // Aggiorna lo status del ticket
    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Integer id,
            @RequestParam("status") Status status,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Ticket> optionalTicket = ticketService.getTicketById(id);
        if (optionalTicket.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Ticket not found.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            return "redirect:/tickets";
        }

        Ticket ticket = optionalTicket.get();

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());

        if (ticket.getOperator() != null &&
                loggedOperator.isPresent() &&
                ticket.getOperator().getId().equals(loggedOperator.get().getId())) {

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

    // Elimina un ticket
    @PostMapping("/delete/{id}")
    public String deleteTicket(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        try {
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("message", "Ticket deleted successfully.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "Ticket with ID " + id + " not found.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/tickets";
    }

}
