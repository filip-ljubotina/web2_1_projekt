package com.web2_1_projekt_be.controller;

import com.web2_1_projekt_be.dto.TicketDto;
import com.web2_1_projekt_be.dto.TicketRequest;
import com.web2_1_projekt_be.entity.Ticket;
import com.web2_1_projekt_be.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TicketController {

    TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping(value = "/generateTicket", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateTicket(@RequestBody TicketRequest ticketRequest) {
        byte[] qrCodeImage  = ticketService.generateTicket(
                ticketRequest.getVatin(),
                ticketRequest.getFirstName(),
                ticketRequest.getLastName()
        );
        return ResponseEntity.ok(qrCodeImage );
    }

    @GetMapping(value="/totalTickets")
    public ResponseEntity<Integer> getTotalTickets(){
        return ResponseEntity.ok(ticketService.getTotalTickets());
    }

    @GetMapping(value="/getTicketByUuid/{uuid}")
    public ResponseEntity<TicketDto> getTicketByUuid(@PathVariable UUID uuid){
        TicketDto ticket =  ticketService.getTicketByUuid(uuid);
        return ResponseEntity.ok(ticket);
    }
}
