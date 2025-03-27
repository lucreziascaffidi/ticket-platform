package com.bept4.ticketplatform.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.service.OperatorService;

@ControllerAdvice
public class BaseController {

    @Autowired
    private OperatorService operatorService;

    @ModelAttribute
    public void addLoggedOperatorToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            Optional<Operator> operator = operatorService.getOperatorByUsername(userDetails.getUsername());
            if (operator.isPresent()) {
                model.addAttribute("loggedOperator", operator.get());
                model.addAttribute("profileImage", operator.get().getProfileImage());
                model.addAttribute("isAdmin", userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            }
        } else {
            model.addAttribute("loggedOperator", null);
            model.addAttribute("profileImage", null);
            model.addAttribute("isAdmin", false);
        }
    }
}
