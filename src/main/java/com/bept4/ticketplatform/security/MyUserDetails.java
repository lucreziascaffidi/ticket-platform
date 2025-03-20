package com.bept4.ticketplatform.security;

import com.bept4.ticketplatform.model.Operator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class MyUserDetails implements UserDetails {

    private Operator operator;

    public MyUserDetails(Operator operator) {
        this.operator = operator;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return operator.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return operator.getPassword(); // Restituisce la password cifrata dell'operatore
    }

    @Override
    public String getUsername() {
        return operator.getUsername(); // Restituisce il nome utente
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Puoi modificare questa logica se desideri
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Puoi modificare questa logica se desideri
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puoi modificare questa logica se desideri
    }

    @Override
    public boolean isEnabled() {
        return operator.isPersonalStatus(); // Puoi personalizzare questa logica (ad esempio per il "non disponibile")
    }

    public Operator getOperator() {
        return operator;
    }
}
