package com.bept4.ticketplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Role;
import com.bept4.ticketplatform.repository.OperatorRepository;
import com.bept4.ticketplatform.repository.RoleRepository;

@Service
public class OperatorRoleService {

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private RoleRepository rolesRepository;

    public void assignRoleToOperator(Integer operatorId, String roleName) {
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operatore non trovato"));

        Role role = rolesRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Ruolo non trovato"));

        operator.getRoles().add(role);

        operatorRepository.save(operator);
    }
}