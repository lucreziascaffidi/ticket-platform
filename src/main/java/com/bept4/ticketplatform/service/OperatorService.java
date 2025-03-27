package com.bept4.ticketplatform.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bept4.ticketplatform.exception.ResourceNotFoundException;
import com.bept4.ticketplatform.model.Operator;
import com.bept4.ticketplatform.model.Role;
import com.bept4.ticketplatform.model.Status;
import com.bept4.ticketplatform.repository.OperatorRepository;
import com.bept4.ticketplatform.repository.RoleRepository;

@Service
public class OperatorService {

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<Operator> getAvailableOperators() {
        return operatorRepository.findAll().stream()
                .filter(Operator::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Operator> getAllOperators() {
        return operatorRepository.findAll();
    }

    public Optional<Operator> getOperatorByUsername(String username) {
        return operatorRepository.findByUsername(username);
    }

    public Optional<Operator> getOperatorByEmail(String email) {
        return operatorRepository.findByEmail(email);
    }

    public Optional<Operator> getOperatorById(Integer id) {
        return operatorRepository.findById(id);
    }

    public void updateProfileImage(String username, String profileImage) {
        operatorRepository.findByUsername(username).ifPresent(op -> {
            op.setProfileImage(profileImage);
            operatorRepository.save(op);
        });
    }

    @Transactional
    public Operator createOperator(Operator operator) {
        operator.setPassword(passwordEncoder.encode(operator.getPassword()));
        return operatorRepository.save(operator);
    }

    @Transactional
    public Operator createOperatorWithDetails(Operator operator, String password, String roleName,
            MultipartFile imageFile)
            throws IOException {

        if (password != null && !password.isBlank()) {
            operator.setPassword(passwordEncoder.encode(password));
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = saveProfileImage(imageFile);
            operator.setProfileImage("/img/profile/" + filename);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        operator.setRoles(roles);

        return operatorRepository.save(operator);
    }

    @Transactional
    public Operator updateOperatorWithRules(Operator updated, Operator actor, boolean isAdmin) {
        Operator existing = operatorRepository.findById(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        if (!isAdmin && !actor.getId().equals(updated.getId())) {
            throw new RuntimeException("Unauthorized update");
        }

        boolean canUpdateStatus = isAdmin || actor.getId().equals(updated.getId());
        if (canUpdateStatus) {
            boolean hasActiveTickets = existing.getTickets().stream()
                    .anyMatch(t -> t.getStatus() == Status.TO_DO || t.getStatus() == Status.IN_PROGRESS);
            if (hasActiveTickets && !updated.isAvailable()) {
                throw new RuntimeException("You have active tickets and can't go offline");
            }
            existing.setAvailable(updated.isAvailable());
        }

        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            updated.setPassword(passwordEncoder.encode(updated.getPassword()));
        } else {
            updated.setPassword(existing.getPassword());
        }

        existing.setUsername(updated.getUsername());
        existing.setEmail(updated.getEmail());
        existing.setProfileImage(updated.getProfileImage());
        existing.setPassword(updated.getPassword());

        return operatorRepository.save(existing);
    }

    @Transactional
    public Operator updateOperatorWithImageAndPassword(Operator updated, String password, MultipartFile imageFile,
            Operator actor, boolean isAdmin, String roleName) throws IOException {

        Operator existing = operatorRepository.findById(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        if (!isAdmin && !actor.getId().equals(updated.getId())) {
            throw new RuntimeException("Unauthorized update");
        }

        existing.setUsername(updated.getUsername());
        existing.setEmail(updated.getEmail());

        if (actor.getId().equals(updated.getId())) {
            boolean hasActiveTickets = existing.getTickets().stream()
                    .anyMatch(t -> t.getStatus() == Status.TO_DO || t.getStatus() == Status.IN_PROGRESS);
            if (!updated.isAvailable() && hasActiveTickets) {
                throw new RuntimeException("You have active tickets and can't go offline");
            }
            existing.setAvailable(updated.isAvailable());
        }

        if (password != null && !password.isBlank()) {
            existing.setPassword(passwordEncoder.encode(password));
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = saveProfileImage(imageFile);
            existing.setProfileImage("/img/profile/" + filename);
        }

        if (isAdmin && roleName != null && !roleName.isBlank()) {
            Role roleEntity = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            Set<Role> newRoles = new HashSet<>();
            newRoles.add(roleEntity);
            existing.setRoles(newRoles);
        }

        System.out.println("📦 Valori salvati:");
        System.out.println("Username: " + existing.getUsername());
        System.out.println("Email: " + existing.getEmail());
        System.out.println("Available: " + existing.isAvailable());
        System.out.println("Profile image: " + existing.getProfileImage());
        System.out.println("Password: " + existing.getPassword());
        System.out.println("Ruoli: " + existing.getRoles());

        return operatorRepository.save(existing);
    }

    @Transactional
    public void updateAvailability(Integer id, boolean isAvailable) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));
        operator.setAvailable(isAvailable);
        operatorRepository.save(operator);
    }

    @Transactional
    public void deleteOperator(Integer id) {
        operatorRepository.deleteById(id);
    }

    private String saveProfileImage(MultipartFile file) throws IOException {
        String uploadDir = "src/main/resources/static/img/profile";
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), path);
        return filename;
    }
}
