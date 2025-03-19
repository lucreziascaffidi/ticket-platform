package com.bept4.ticketplatform.model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "operators")
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String username;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Email should be valid")
    private String email;

    private boolean personalStatus;

    @OneToMany(mappedBy = "operator")
    private Set<Ticket> tickets;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "operator_roles", joinColumns = @JoinColumn(name = "operator_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Roles> roles;

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    // Getters e Setters
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPersonalStatus() {
        return this.personalStatus;
    }

    public void setPersonalStatus(boolean personalStatus) {
        this.personalStatus = personalStatus;
    }

    public Set<Ticket> getTickets() {
        return this.tickets;
    }

    public Set<Roles> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Roles> roles) {
        this.roles = roles;
    }

}
