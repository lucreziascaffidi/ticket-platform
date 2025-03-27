package com.bept4.ticketplatform.controller;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Status;
import com.bept4.ticketplatform.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/home")
    public String home(@RequestParam(value = "loginSuccess", required = false) String loginSuccess,
            Model model) {

        Operator loggedOperator = (Operator) model.getAttribute("loggedOperator");
        Boolean isAdminAttr = (Boolean) model.getAttribute("isAdmin");
        boolean isAdmin = Boolean.TRUE.equals(isAdminAttr);

        if (loggedOperator == null) {
            return "redirect:/login";
        }

        model.addAttribute("current", "Home");

        if (loginSuccess != null) {
            model.addAttribute("successMessage", "Login successful!");
        }

        if (isAdmin) {
            model.addAttribute("totalTickets", ticketService.countAllTickets());
            model.addAttribute("toDoTickets", ticketService.countByStatus(Status.TO_DO));
            model.addAttribute("inProgressTickets", ticketService.countByStatus(Status.IN_PROGRESS));
            model.addAttribute("doneTickets", ticketService.countByStatus(Status.COMPLETED));
        } else {
            model.addAttribute("totalTickets", ticketService.countByOperator(loggedOperator));
            model.addAttribute("toDoTickets", ticketService.countByOperatorAndStatus(loggedOperator, Status.TO_DO));
            model.addAttribute("inProgressTickets",
                    ticketService.countByOperatorAndStatus(loggedOperator, Status.IN_PROGRESS));
            model.addAttribute("doneTickets", ticketService.countByOperatorAndStatus(loggedOperator, Status.COMPLETED));
        }

        return "home";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }
}
