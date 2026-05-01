package ru.ivanov.diplom.inventory_system.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TemporaryPasswordGenerator {
    private static final String SYMBOLS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private static final int DEFAULT_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            int index = random.nextInt(SYMBOLS.length());
            result.append(SYMBOLS.charAt(index));
        }

        return result.toString();
    }
}
