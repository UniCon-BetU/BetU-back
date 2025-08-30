package org.example.general.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        var sender = new org.springframework.mail.javamail.JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");      // 필요시 host/port 설정
        sender.setPort(587);
        sender.setUsername("koreabetu@gmail.com");
        sender.setPassword("cqoikxxynebfcthe");

        var props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true"); // 디버그 로그 보고 싶으면
        return sender;
    }
}