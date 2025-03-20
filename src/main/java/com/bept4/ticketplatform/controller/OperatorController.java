package com.bept4.ticketplatform.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bept4.ticketplatform.exception.ResourceNotFoundException;
import com.bept4.ticketplatform.exception.ValidationException;
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
        try {
            Operator createdOperator = operatorService.createOperator(operator);
            return ResponseEntity.ok(createdOperator);
        } catch (Exception e) {
            throw new ValidationException("Errore nella creazione dell'operatore");
        }
    }

    // Ottieni un operatore per username
    @GetMapping("/{username}")
    public ResponseEntity<Operator> getOperatorByUsername(@PathVariable String username) {
        Optional<Operator> operator = operatorService.getOperatorByUsername(username);
        if (operator.isEmpty()) {
            throw new ResourceNotFoundException("Operatore con username " + username + " non trovato");
        }
        return ResponseEntity.ok(operator.get());
    }

    // Aggiorna un operatore
    @PutMapping("/{id}")
    public ResponseEntity<Operator> updateOperator(@PathVariable Integer id, @Valid @RequestBody Operator operator) {
        operator.setId(id);
        Operator updatedOperator = operatorService.updateOperator(operator);
        if (updatedOperator == null) {
            throw new ResourceNotFoundException("Operatore con ID " + id + " non trovato per l'aggiornamento");
        }
        return ResponseEntity.ok(updatedOperator);
    }

    // Elimina un operatore
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperator(@PathVariable Integer id) {
        operatorService.deleteOperator(id); // Non assegnare il risultato a una variabile
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/operator/{username}")
    public String viewOperatorProfile(@PathVariable String username, Model model) {
        Optional<Operator> operator = operatorService.getOperatorByUsername(username);
        if (operator.isPresent()) {
            model.addAttribute("operator", operator.get());
            // Imposta un'immagine di default se l'operatore non ha un'immagine
            String profileImage = operator.get().getProfileImage();
            if (profileImage == null || profileImage.isEmpty()) {
                profileImage = "/img/default.png"; // Immagine di profilo predefinita
            }
            model.addAttribute("profileImage", profileImage);
        }
        return "operator-profile"; // Modifica con il nome della tua pagina
    }

    @PostMapping("/operator/{username}/updateProfileImage")
    public String updateProfileImage(@PathVariable String username, @RequestParam String profileImage, Model model) {
        operatorService.updateProfileImage(username, profileImage);
        return "redirect:/operator/" + username; // Dopo l'aggiornamento, torna alla pagina del profilo
    }

}
