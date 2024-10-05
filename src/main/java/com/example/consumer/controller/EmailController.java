package com.example.consumer.controller;

import com.example.consumer.dto.notification.MessageDTO;
import com.example.consumer.dto.notification.NotificationDTO;
import com.example.consumer.service.EmailService;
import com.example.consumer.validators.EmailValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class EmailController {

    private final EmailService emailService;

    private final EmailValidator emailValidator;

    public EmailController(EmailService emailService, EmailValidator emailValidator) {
        this.emailService = emailService;
        this.emailValidator = emailValidator;
    }

    @PostMapping("/send-email")
    public ResponseEntity<MessageDTO> sendEmail(@RequestHeader("Authorization") String authorizationHeader,
                                                @RequestBody NotificationDTO requestDto) {

        if (!emailValidator.isValidAuthorizationToken(authorizationHeader, requestDto.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!emailValidator.isValidPayload(requestDto)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        boolean emailSent = emailService.sendEmail(requestDto);
        if (emailSent) {
            return ResponseEntity.ok(new MessageDTO("Mail trimis catre " + requestDto.getEmail()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/send-email/report")
    public ResponseEntity<String> handleFileUpload(@RequestHeader("Authorization") String authorizationHeader,
                                                   @RequestParam("requestDto") String requestDtoStr,
                                                   @RequestParam("file") MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationDTO requestDto;
        try {
            requestDto = objectMapper.readValue(requestDtoStr, NotificationDTO.class);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid requestDto format.");
        }

        if (!emailValidator.isValidAuthorizationToken(authorizationHeader, requestDto.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!emailValidator.isValidPayload(requestDto)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fisierul nu poate fi gol.");
        }
        this.emailService.saveFileAndSendEmail(file, requestDto);
        return ResponseEntity.ok("Fisierul a fost incarcat si trimis cu succes catre " + requestDto.getEmail());
    }

}
