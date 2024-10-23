package com.web2_1_projekt_be.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.web2_1_projekt_be.dto.TicketDto;
import com.web2_1_projekt_be.entity.Ticket;
import com.web2_1_projekt_be.repository.TicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    @Value("${FE_URL}")
    private String feUrl;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Integer getTotalTickets(){
        return (int) ticketRepository.count();
    }

    public byte[] generateTicket(String vatin, String firstName, String lastName) {

        if (vatin == null || firstName == null || lastName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nisu predani svi ulazni podaci");
        }

        if (ticketRepository.countByVatin(vatin) >= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maksimalno 3 ulaznice po OIB-u");
        }

        Ticket ticket = new Ticket();
        ticket.setVatin(vatin);
        ticket.setFirstName(firstName);
        ticket.setLastName(lastName);
        ticket.setCreatedAt(LocalDateTime.now());
        Ticket genTicket = ticketRepository.save(ticket);

        String url = feUrl + "/private?uuid=" + genTicket.getId().toString();

        try {
            return generateQRCodeImage(url, 350, 350);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating QR code");
        }
    }

    private byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, java.io.IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);

        return baos.toByteArray();
    }

    public TicketDto getTicketByUuid(UUID uuid) {
           Ticket ticket = ticketRepository.findById(uuid).get();
           return new TicketDto(ticket.getId(), ticket.getVatin(), ticket.getFirstName(), ticket.getLastName(), ticket.getCreatedAt());
        }
        
}
