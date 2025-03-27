package com.bept4.ticketplatform.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bept4.ticketplatform.exception.ResourceNotFoundException;
import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Role;
import com.bept4.ticketplatform.model.Status;
import com.bept4.ticketplatform.service.OperatorService;
import com.bept4.ticketplatform.service.TicketService;

@Controller
@RequestMapping("/operators")
public class OperatorController {

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private TicketService ticketService;

    // Mostra lista operatori
    @GetMapping
    public String viewOperatorsIndex(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        if (loggedOperator.isEmpty())
            return "redirect:/login";

        model.addAttribute("loggedOperator", loggedOperator.get());

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin)
            return "redirect:/operators/profile";

        List<Operator> sortedOperators = operatorService.getAllOperators().stream()
                .sorted((a, b) -> {
                    boolean aIsAdmin = a.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
                    boolean bIsAdmin = b.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
                    return Boolean.compare(!aIsAdmin, !bIsAdmin);
                }).toList();

        model.addAttribute("admin", loggedOperator);
        model.addAttribute("allOperators", sortedOperators);
        return "operators/index";
    }

    // Profilo dell'utente loggato
    @GetMapping("/profile")
    public String getProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        if (loggedOperator.isEmpty())
            return "redirect:/login";

        Operator operator = loggedOperator.get();
        model.addAttribute("loggedOperator", operator);
        model.addAttribute("operator", operator);
        model.addAttribute("assignedTickets", ticketService.getTicketsByOperator(operator));

        boolean hasPendingTickets = operator.getTickets().stream()
                .anyMatch(t -> t.getStatus() == Status.TO_DO || t.getStatus() == Status.IN_PROGRESS);
        model.addAttribute("canToggleStatus", !hasPendingTickets || !operator.isAvailable());

        return "operators/show";
    }

    // Visualizza un operatore per ID (solo admin o se stesso)
    @GetMapping("/{id}")
    public String getOperatorById(@PathVariable Integer id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        if (loggedOperator.isEmpty())
            return "redirect:/login";

        Operator viewer = loggedOperator.get();
        model.addAttribute("loggedOperator", viewer);

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        if (!isAdmin && !viewer.getId().equals(id)) {
            return "redirect:/operators/profile";
        }

        Operator operator = operatorService.getOperatorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));
        model.addAttribute("operator", operator);
        model.addAttribute("assignedTickets", ticketService.getTicketsByOperator(operator));

        boolean hasPendingTickets = operator.getTickets().stream()
                .anyMatch(t -> t.getStatus() == Status.TO_DO || t.getStatus() == Status.IN_PROGRESS);
        model.addAttribute("canToggleStatus", !hasPendingTickets || !operator.isAvailable());

        return "operators/show";
    }

    // Cambia lo status dell'utente se non ha ticket attivi
    @PostMapping("/status/update")
    public String updateAvailability(@RequestParam Integer id,
            @RequestParam(required = false) Boolean available,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        if (loggedOperator.isEmpty())
            return "redirect:/login";

        Operator current = loggedOperator.get();

        if (!current.getId().equals(id)) {
            return "redirect:/operators/profile";
        }

        boolean hasActiveTickets = current.getTickets().stream()
                .anyMatch(t -> t.getStatus() == Status.TO_DO || t.getStatus() == Status.IN_PROGRESS);

        if (!Boolean.TRUE.equals(available) && hasActiveTickets) {
            redirectAttributes.addFlashAttribute("message",
                    "You can't deactivate yourself while you have active tickets.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            return "redirect:/operators/profile";
        }

        operatorService.updateAvailability(id, Boolean.TRUE.equals(available));
        redirectAttributes.addFlashAttribute("message", "Status updated successfully.");
        redirectAttributes.addFlashAttribute("messageClass", "alert-success");

        return "redirect:/operators/profile";
    }

    // Mostra il form di creazione di un nuovo operatore
    @GetMapping("/create")
    public String createOperatorForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin)
            return "redirect:/operators/profile";

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

        Operator operator = new Operator();
        operator.setRoles(new HashSet<>());

        model.addAttribute("operator", operator);
        model.addAttribute("isAdmin", true);
        model.addAttribute("create", true);
        model.addAttribute("roles", List.of("ADMIN", "USER"));
        model.addAttribute("loggedOperator", loggedOperator.orElse(null));

        return "operators/create-or-edit";
    }

    // Salva nuovo operatore
    @PostMapping("/create")
    public String createOperator(@ModelAttribute Operator operator,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        try {
            operatorService.createOperatorWithDetails(operator, password, role, imageFile);
            redirectAttributes.addFlashAttribute("message", "Operator created successfully.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error creating operator.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/operators";
    }

    // Apre il form precompilato con i dati dell’operatore
    @GetMapping("/edit/{id}")
    public String editOperatorForm(@PathVariable Integer id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return "redirect:/login";

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        if (loggedOperator.isEmpty())
            return "redirect:/login";

        Operator viewer = loggedOperator.get();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !viewer.getId().equals(id)) {
            return "redirect:/operators/profile";
        }

        Operator operator = operatorService.getOperatorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        if (operator.getRoles() == null) {
            operator.setRoles(new HashSet<>());
        }

        boolean hasPendingTickets = operator.getTickets().stream()
                .anyMatch(t -> t.getStatus() == Status.TO_DO || t.getStatus() == Status.IN_PROGRESS);

        model.addAttribute("operator", operator);
        model.addAttribute("create", false);
        model.addAttribute("canToggleStatus", !hasPendingTickets);
        model.addAttribute("loggedOperator", viewer);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("roles", List.of("ADMIN", "USER"));

        String selectedRole = operator.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("");

        model.addAttribute("selectedRole", selectedRole);

        return "operators/create-or-edit";
    }

    // Aggiorna le informazioni di un operatore esistente
    @PostMapping("/edit/{id}")
    public String updateOperator(
            @PathVariable Integer id,
            @ModelAttribute Operator operator,
            @RequestParam("password") String password,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "role", required = false) String role,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Optional<Operator> loggedOperator = operatorService.getOperatorByUsername(userDetails.getUsername());
        if (loggedOperator.isEmpty())
            return "redirect:/login";

        Operator viewer = loggedOperator.get();

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !viewer.getId().equals(id)) {
            return "redirect:/operators/profile";
        }

        try {
            operator.setId(id);
            operatorService.updateOperatorWithImageAndPassword(
                    operator, password, imageFile, viewer, isAdmin, role);

            redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "An error occurred while updating.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/operators/" + id;
    }

    // Elimina un operatore
    @PostMapping("/delete/{id}")
    public String deleteOperator(@PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin)
            return "redirect:/operators/profile";

        Optional<Operator> operatorOpt = operatorService.getOperatorById(id);

        if (operatorOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Operator not found.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            return "redirect:/operators";
        }

        Operator operator = operatorOpt.get();

        if (operator.getTickets() != null && !operator.getTickets().isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Cannot delete operator with assigned tickets.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
            return "redirect:/operators";
        }

        operatorService.deleteOperator(id);
        redirectAttributes.addFlashAttribute("message", "Operator deleted successfully.");
        redirectAttributes.addFlashAttribute("messageClass", "alert-success");

        return "redirect:/operators";
    }
}
