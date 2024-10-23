package com.web2_1_projekt_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TicketDto {
    private UUID id;
    private String vatin;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
}
