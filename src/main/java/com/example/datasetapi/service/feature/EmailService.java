package com.example.datasetapi.service.feature;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {

    void sendAccountInfoEmail(String to, String username, String password, String resetLink);

    void sendVerfiMail(String s, String email);
}
