package com.example.farmaventa.modelo;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private final Session session;
    private final String fromEmail;

    public EmailService(String host, String port, String fromEmail, String password) {
        this.fromEmail = fromEmail;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        session.setDebug(true);
    }

    public void enviar(String to, String subject, String body) throws MessagingException {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setText(body);
        Transport.send(msg);
    }

    public void enviarConReporte(String to, String subject, String body,
                                 String pdfName, InputStream pdfData) throws Exception {
        byte[] pdfBytes = pdfData.readAllBytes();

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(body);

        MimeBodyPart attachPart = new MimeBodyPart();
        attachPart.setDataHandler(new DataHandler(
                new ByteArrayDataSource(pdfBytes, "application/pdf")));
        attachPart.setFileName(pdfName);

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(textPart);
        mp.addBodyPart(attachPart);

        msg.setContent(mp);
        Transport.send(msg);
    }
}
