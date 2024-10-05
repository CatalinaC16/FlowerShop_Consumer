package com.example.consumer.config;

import com.example.consumer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueListenerConfig {

    @Autowired
    EmailService emailService;

    @Bean
    public QueueListener queueListenerService() {
        QueueListener listener = new QueueListener(emailService);
        listener.startListening();
        return listener;
    }
}

