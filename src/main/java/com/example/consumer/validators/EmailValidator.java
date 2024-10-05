package com.example.consumer.validators;

import com.example.consumer.dto.notification.InvoiceDTO;
import com.example.consumer.dto.notification.NotificationDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmailValidator {

    public boolean isValidAuthorizationToken(String authorizationHeader, UUID id_user) {
        return authorizationHeader != null && authorizationHeader.equals("Bearer flowerShop" + id_user.toString() + id_user.toString());
    }

    public boolean isValidPayload(NotificationDTO requestDto) {
        return requestDto != null
                && requestDto.getId() != null
                && requestDto.getName() != null
                && requestDto.getEmail() != null
                && requestDto.getBodyAction() != null
                && requestDto.getBody() != null;
    }

    public boolean isValidPayloadForInvoice(InvoiceDTO invoiceDTO) {
        return invoiceDTO != null
                && invoiceDTO.getId() != null
                && invoiceDTO.getEmail() != null
                && invoiceDTO.getBody() != null;
    }
}
