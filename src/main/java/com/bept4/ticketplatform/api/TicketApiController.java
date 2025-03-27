package com.bept4.ticketplatform.api;

import com.bept4.ticketplatform.model.Status;
import com.bept4.ticketplatform.model.Ticket;
import com.bept4.ticketplatform.model.Category;
import com.bept4.ticketplatform.service.CategoryService;
import com.bept4.ticketplatform.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketApiController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Ticket> getTickets(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status) {

        if (categoryId != null && status != null) {
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isEmpty()) {
                throw new IllegalArgumentException("Categoria non trovata");
            }
            return ticketService.getTicketsByFilters(null, categoryId, status);
        } else if (categoryId != null) {
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isEmpty()) {
                throw new IllegalArgumentException("Categoria non trovata");
            }
            return ticketService.getTicketsByCategory(categoryOpt.get());
        } else if (status != null) {
            return ticketService.getTicketsByStatus(Status.valueOf(status.toUpperCase()));
        }

        return ticketService.getAllTickets();
    }
}
