package com.bept4.ticketplatform.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.service.OperatorService;

@Controller
public class BaseController {

    @Autowired
    private OperatorService operatorService;

    @ModelAttribute
    public void addLoggedUserToModel(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            Optional<Operator> operator = operatorService.getOperatorByUsername(userDetails.getUsername());
            if (operator.isPresent()) {
                model.addAttribute("loggedUser", operator.get());
                model.addAttribute("profileImage", operator.get().getProfileImage());
            }
        } else {
            model.addAttribute("loggedUser", null);
            model.addAttribute("profileImage", null);
        }
    }
}
