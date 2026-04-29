package ru.ivanov.diplom.inventory_system.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DocumentNumberGenerator {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AtomicLong counter = new AtomicLong(1);

    public String generate(String documentTypeCode) {
        return documentTypeCode + "-"
                + LocalDateTime.now().format(FORMATTER)
                + "-"
                + counter.getAndIncrement();
    }
}
