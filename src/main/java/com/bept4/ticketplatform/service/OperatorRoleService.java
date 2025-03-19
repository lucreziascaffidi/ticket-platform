package com.bept4.ticketplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Roles;
import com.bept4.ticketplatform.repository.OperatorRepository;
import com.bept4.ticketplatform.repository.RolesRepository;

@Service
public class OperatorRoleService {

    @Autowired
    private OperatorRepository operatorRepository; // Repository degli operatori

    @Autowired
    private RolesRepository rolesRepository; // Repository dei ruoli

    // Metodo per collegare un ruolo all'operatore
    public void assignRoleToOperator(Integer operatorId, String roleName) {
        // Trova l'operatore dal database
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operatore non trovato"));

        // Trova il ruolo dal database
        Roles role = rolesRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Ruolo non trovato"));

        // Aggiungi il ruolo all'operatore
        operator.getRoles().add(role);

        // Salva l'operatore aggiornato
        operatorRepository.save(operator);
    }
}