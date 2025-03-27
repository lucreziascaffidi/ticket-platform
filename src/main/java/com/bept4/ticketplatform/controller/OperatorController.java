package com.bept4.ticketplatform.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping
    public String viewOperatorsIndex(Model model) {
        Operator loggedOperator = (Operator) model.getAttribute("loggedOperator");
        Boolean isAdmin = (Boolean) model.getAttribute("isAdmin");

        if (loggedOperator == null)
            return "redirect:/login";
        if (!Boolean.TRUE.equals(isAdmin))
            return "redirect:/operators/profile";

        List<Operator> sortedOperators = operatorService.getAllOperators().stream()
                .sorted((a, b) -> {
                    boolean aIsAdmin = a.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
                    boolean bIsAdmin = b.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
                    return Boolean.compare(!aIsAdmin, !bIsAdmin);
                }).toList();

        model.addAttribute("allOperators", sortedOperators);
        return "operators/index";
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        Operator operator = (Operator) model.getAttribute("loggedOperator");
        if (operator == null)
            return "redirect:/login";

        model.addAttribute("operator", operator);
        model.addAttribute("assignedTickets", ticketService.getTicketsByOperator(operator));

        boolean hasPendingTickets = operator.getTickets().stream()
                .anyMatch(t -> t.getStatus() == Status.TO_DO || t.getStatus() == Status.IN_PROGRESS);
        model.addAttribute("canToggleStatus", !hasPendingTickets || !operator.isAvailable());

        return "operators/show";
    }

    @GetMapping("/{id}")
    public String getOperatorById(@PathVariable Integer id, Model model) {
        Operator viewer = (Operator) model.getAttribute("loggedOperator");
        Boolean isAdmin = (Boolean) model.getAttribute("isAdmin");

        if (viewer == null)
            return "redirect:/login";
        if (!Boolean.TRUE.equals(isAdmin) && !viewer.getId().equals(id)) {
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

    @PostMapping("/status/update")
    public String updateAvailability(@RequestParam Integer id,
            @RequestParam(required = false) Boolean available,
            Model model,
            RedirectAttributes redirectAttributes) {

        Operator current = (Operator) model.getAttribute("loggedOperator");
        if (current == null || !current.getId().equals(id)) {
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

    @GetMapping("/create")
    public String createOperatorForm(Model model) {
        Boolean isAdmin = (Boolean) model.getAttribute("isAdmin");
        if (!Boolean.TRUE.equals(isAdmin))
            return "redirect:/operators/profile";

        Operator operator = new Operator();
        operator.setRoles(new HashSet<>());

        model.addAttribute("operator", operator);
        model.addAttribute("isAdmin", true);
        model.addAttribute("create", true);
        model.addAttribute("roles", List.of("ADMIN", "USER"));

        return "operators/create-or-edit";
    }

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

    @GetMapping("/edit/{id}")
    public String editOperatorForm(@PathVariable Integer id, Model model) {
        Operator viewer = (Operator) model.getAttribute("loggedOperator");
        Boolean isAdmin = (Boolean) model.getAttribute("isAdmin");

        if (viewer == null || (!Boolean.TRUE.equals(isAdmin) && !viewer.getId().equals(id))) {
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
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("roles", List.of("ADMIN", "USER"));

        String selectedRole = operator.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("");

        model.addAttribute("selectedRole", selectedRole);

        return "operators/create-or-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateOperator(@PathVariable Integer id,
            @ModelAttribute Operator operator,
            @RequestParam("password") String password,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "role", required = false) String role,
            Model model,
            RedirectAttributes redirectAttributes) {

        Operator viewer = (Operator) model.getAttribute("loggedOperator");
        Boolean isAdminAttr = (Boolean) model.getAttribute("isAdmin");
        boolean isAdmin = Boolean.TRUE.equals(isAdminAttr); // Safe conversion

        if (viewer == null || (!isAdmin && !viewer.getId().equals(id))) {
            return "redirect:/operators/profile";
        }

        try {
            operator.setId(id);
            operatorService.updateOperatorWithImageAndPassword(
                    operator, password, imageFile, viewer, isAdmin, role);

            redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "An error occurred while updating.");
            redirectAttributes.addFlashAttribute("messageClass", "alert-danger");
        }

        return "redirect:/operators/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteOperator(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Boolean isAdmin = (Boolean) model.getAttribute("isAdmin");
        if (!Boolean.TRUE.equals(isAdmin))
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
