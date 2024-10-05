package com.example.consumer.service;

import com.example.consumer.dto.notification.InvoiceDTO;
import com.example.consumer.dto.notification.NotificationDTO;
import com.example.consumer.validators.EmailValidator;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    private final EmailValidator emailValidator;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender javaMailSender, EmailValidator emailValidator) {
        this.javaMailSender = javaMailSender;
        this.emailValidator = emailValidator;
    }

    public boolean sendEmail(NotificationDTO notificationRequestDto) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(notificationRequestDto.getEmail());
            helper.setSubject(notificationRequestDto.getBodyAction() + " on FlowerShop");

            String htmlBody = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/accountCreated.html")));

            setEmailTemplate(helper, htmlBody);

            javaMailSender.send(mimeMessage);
            return true;
        } catch (MessagingException e) {
            LOGGER.error("Ceva nu a mers bine la crearea email-ului", e);
        } catch (IOException e) {
            LOGGER.error("Nu s-a citit fisierul HTML pentru construirea emailului", e);
        }
        return false;
    }

    public void sendEmailWithPdf(InvoiceDTO invoiceDTO) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            if (this.emailValidator.isValidPayloadForInvoice(invoiceDTO)) {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(invoiceDTO.getEmail());
                helper.setSubject("Invoice");

                String htmlBody = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/invoiceSent.html")));
                setEmailTemplate(helper, htmlBody);

                OutputStream out = new FileOutputStream("invoice_user.pdf");
                out.write(invoiceDTO.getBody());
                out.close();
                DataSource pdf = new FileDataSource("invoice_user.pdf");
                helper.addAttachment("invoice_user.pdf", pdf);
                javaMailSender.send(message);
                LOGGER.info("Email-ul a fost trimis catre {}", invoiceDTO.getEmail());
            } else {
                LOGGER.error("Payload-ul nu este valid");
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private void setEmailTemplate(MimeMessageHelper helper, String htmlBody) throws MessagingException {
        helper.setText(htmlBody, true);
        DataSource cornerImg = new FileDataSource("src/main/resources/templates/corner.png");
        helper.addInline("corner", cornerImg);
        DataSource cornerRightImg = new FileDataSource("src/main/resources/templates/corner-right.png");
        helper.addInline("corner-right", cornerRightImg);
        DataSource bouquet = new FileDataSource("src/main/resources/templates/bouquet1.png");
        helper.addInline("bouquet1", bouquet);
    }

    public void saveFileAndSendEmail(MultipartFile file, NotificationDTO requestDto) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            this.saveFile(file);
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(requestDto.getEmail());
            helper.setSubject("Raport");
            String htmlBody = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/reportSent.html")));
            setEmailTemplate(helper, htmlBody);
            String fileName = file.getOriginalFilename();
            String filePath = "src/main/resources/" + fileName;
            File attachment = new File(filePath);
            if (attachment.exists()) {
                helper.addAttachment(fileName, attachment);
            }
            javaMailSender.send(message);
            LOGGER.info("Email-ul a fost trimis catre {}", requestDto.getEmail());
        } catch (MessagingException |
                 IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String directory = "src/main/resources";
        File uploadDir = new File(directory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = new FileOutputStream(new File(directory, fileName))) {
            int readBytes;
            byte[] buffer = new byte[1024];
            while ((readBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readBytes);
            }
        }
    }
}
