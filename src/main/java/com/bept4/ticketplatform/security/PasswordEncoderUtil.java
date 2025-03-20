package com.bept4.ticketplatform.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        String password = "4567"; // La password che vuoi criptare
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);

        System.out.println(hashedPassword); // Questo è l'hash che dovrai salvare nel DB
    }
}