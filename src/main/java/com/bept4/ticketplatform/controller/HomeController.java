package com.bept4.ticketplatform.controller;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Status;
import com.bept4.ticketplatform.service.OperatorService;
import com.bept4.ticketplatform.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/home")
    public String home(@RequestParam(value = "loginSuccess", required = false) String loginSuccess,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null)
            return "redirect:/login";

        Optional<Operator> operatorOpt = operatorService.getOperatorByUsername(userDetails.getUsername());
        if (operatorOpt.isEmpty())
            return "redirect:/login";

        Operator loggedUser = operatorOpt.get();
        model.addAttribute("loggedOperator", loggedUser);
        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("current", "Home");

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        if (loginSuccess != null) {
            model.addAttribute("successMessage", "Hai effettuato correttamente il login!");
        }

        if (isAdmin) {
            model.addAttribute("totalTickets", ticketService.countAllTickets());
            model.addAttribute("toDoTickets", ticketService.countByStatus(Status.TO_DO));
            model.addAttribute("inProgressTickets", ticketService.countByStatus(Status.IN_PROGRESS));
            model.addAttribute("doneTickets", ticketService.countByStatus(Status.COMPLETED));
        } else {
            model.addAttribute("totalTickets", ticketService.countByOperator(loggedUser));
            model.addAttribute("toDoTickets", ticketService.countByOperatorAndStatus(loggedUser, Status.TO_DO));
            model.addAttribute("inProgressTickets",
                    ticketService.countByOperatorAndStatus(loggedUser, Status.IN_PROGRESS));
            model.addAttribute("doneTickets", ticketService.countByOperatorAndStatus(loggedUser, Status.COMPLETED));
        }

        return "home";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }
}