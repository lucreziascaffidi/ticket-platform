package com.bept4.ticketplatform.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.repository.OperatorRepository;

import jakarta.transaction.Transactional;

@Service
public class OperatorService {

    @Autowired
    private OperatorRepository operatorRepository;

    // Crea un nuovo operatore
    @Transactional
    public Operator createOperator(Operator operator) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(operator.getPassword());
        operator.setPassword(encodedPassword);
        return operatorRepository.save(operator);
    }

    // Ottieni un operatore per username
    public Optional<Operator> getOperatorByUsername(String username) {
        return operatorRepository.findByUsername(username);
    }

    // Ottieni un operatore per email
    public Optional<Operator> getOperatorByEmail(String email) {
        return operatorRepository.findByEmail(email);
    }

    // Ottieni un operatore per id
    public Optional<Operator> getOperatorById(Integer id) {
        return operatorRepository.findById(id);
    }

    // Aggiorna i dati di un operatore
    @Transactional
    public Operator updateOperator(Operator operator) {
        if (operator.getPassword() != null) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(operator.getPassword());
            operator.setPassword(encodedPassword);
        }
        return operatorRepository.save(operator);
    }

    // Elimina un operatore
    @Transactional
    public void deleteOperator(Integer id) {
        operatorRepository.deleteById(id);
    }
}
