package com.bept4.ticketplatform.security;

import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.service.OperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    @Autowired
    private OperatorService operatorService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Recupera l'operatore dal database
        Operator operator = operatorService.getOperatorByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Restituisce un oggetto UserDetails
        return new MyUserDetails(operator);
    }
}
