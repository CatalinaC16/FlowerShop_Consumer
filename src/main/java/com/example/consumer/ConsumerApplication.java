package com.example.consumer;

import com.example.consumer.config.QueueListener;
import com.example.consumer.config.QueueListenerConfig;
import com.example.consumer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
        QueueListenerConfig queueListenerService = new QueueListenerConfig();
    }

}
