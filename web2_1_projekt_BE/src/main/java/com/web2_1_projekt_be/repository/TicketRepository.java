package com.web2_1_projekt_be.repository;

import com.web2_1_projekt_be.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    int countByVatin(String vatin);

}