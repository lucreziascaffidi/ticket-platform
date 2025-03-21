package com.bept4.ticketplatform.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.service.OperatorService;

@Controller
public class HomeController {

    @Autowired
    private OperatorService operatorService;

    @GetMapping("/home")
    public String homePage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Optional<Operator> operator = operatorService.getOperatorByUsername(username);
        if (operator.isPresent()) {
            model.addAttribute("operator", operator.get());
        }
        return "home";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }
}
