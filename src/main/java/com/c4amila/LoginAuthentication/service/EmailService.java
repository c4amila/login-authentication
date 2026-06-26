package com.c4amila.LoginAuthentication.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void enviarEmailRecuperacao(String para, String nomeUsuario, String codigoToken){
        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setFrom("nao-responda@sisstema.com");
        msg.setTo(para);
        msg.setSubject("Recuperação de senha");
        msg.setText("Olá " + nomeUsuario + "!\n\nVocê solicitou a recuperação de senha da sua conta.\n" +
                "Use o código abaixo para validar a alteração:\n\n" +
                "Código: " + codigoToken + "\n\n" +
                "Este código expira em breve. Se você não solicitou esta alteração, ignore este e-mail.");

        javaMailSender.send(msg);
    }
}
