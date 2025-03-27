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
        return operator.getPassword();
    }

    @Override
    public String getUsername() {
        return operator.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getProfileImage() {
        return operator != null ? operator.getProfileImage() : "/img/profile/default.png";
    }
}
