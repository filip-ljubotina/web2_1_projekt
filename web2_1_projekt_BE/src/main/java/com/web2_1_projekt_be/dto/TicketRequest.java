package com.web2_1_projekt_be.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TicketRequest {
    private String vatin;
    private String firstName;
    private String lastName;
}
