package com.example.utecharity_project.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendMail(String to, String subject, String message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom("quachkhoa175@gmail.com");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);

            Context context = new Context();
            context.setVariable("message", message);
            String content = templateEngine.process("page_admin/Handle/email-template", context);

            messageHelper.setText(content, true);
            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

