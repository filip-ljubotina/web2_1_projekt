package com.web2_1_projekt_be.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String timestamp;
    private String error;
}
