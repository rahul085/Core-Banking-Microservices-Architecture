package com.example.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;

    public void sendTransferSuccessfulMail(String toEmail,String amount,String receiverName){
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setTo(toEmail);
        simpleMailMessage.setSubject("Transfer Successful");
        simpleMailMessage.setText("Your transfer of " + amount + " to " + receiverName + " was successful.");
        javaMailSender.send(simpleMailMessage);
        log.info("Successfully sent sender confirmation email to {}", toEmail);
    }

    public void sendMoneyReceivedEmail(String toEmail, String amount, String senderName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("You Received Funds!");
        message.setText("You have received $" + amount + " from " + senderName + ".");

        javaMailSender.send(message);
        log.info("Successfully sent receiver confirmation email to {}", toEmail);
    }
}
