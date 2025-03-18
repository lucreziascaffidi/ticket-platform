package com.bept4.ticketplatform.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.service.OperatorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/operators")
public class OperatorController {

    @Autowired
    private OperatorService operatorService;

    // Crea un nuovo operatore
    @PostMapping
    public ResponseEntity<Operator> createOperator(@Valid @RequestBody Operator operator) {
        Operator createdOperator = operatorService.createOperator(operator);
        return ResponseEntity.ok(createdOperator);
    }

    // Ottieni un operatore per username
    @GetMapping("/{username}")
    public ResponseEntity<Operator> getOperatorByUsername(@PathVariable String username) {
        Optional<Operator> operator = operatorService.getOperatorByUsername(username);
        return operator.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Aggiorna un operatore
    @PutMapping("/{id}")
    public ResponseEntity<Operator> updateOperator(@PathVariable Integer id, @Valid @RequestBody Operator operator) {
        operator.setId(id);
        Operator updatedOperator = operatorService.updateOperator(operator);
        return ResponseEntity.ok(updatedOperator);
    }

    // Elimina un operatore
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperator(@PathVariable Integer id) {
        operatorService.deleteOperator(id);
        return ResponseEntity.noContent().build();
    }
}
