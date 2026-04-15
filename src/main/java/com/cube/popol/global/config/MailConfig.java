package com.cube.popol.global.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

  @Value("${spring.mail.host}")
  private String mailHost;
  @Value("${spring.mail.port}")
  private int mailPort;
  @Value("${spring.mail.username}")
  private String mailUsername;
  @Value("${spring.mail.password}")
  private String mailPassword;

  @Value("${spring.mail.properties.mail.smtp.auth}")
  private String auth;
  @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
  private String starttlsEnable;
  @Value("${spring.mail.properties.mail.smtp.starttls.required}")
  private String starttlsRequired;
  @Value("${spring.mail.properties.mail.smtp.connectiontimeout}")
  private String connectionTimeout;
  @Value("${spring.mail.properties.mail.smtp.timeout}")
  private String timeout;
  @Value("${spring.mail.properties.mail.smtp.writetimeout}")
  private String writeTimeout;

  private Properties getMailProperties() {
    Properties properties = new Properties();

    properties.put("mail.smtp.auth", auth);
    properties.put("mail.smtp.starttls.enable", starttlsEnable);
    properties.put("mail.smtp.starttls.required", starttlsRequired);
    properties.put("mail.smtp.connectiontimeout", connectionTimeout);
    properties.put("mail.smtp.timeout", timeout);
    properties.put("mail.smtp.writetimeout", writeTimeout);

    return properties;
  }


  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailHost);
    mailSender.setPort(mailPort);
    mailSender.setUsername(mailUsername);
    mailSender.setPassword(mailPassword);
    mailSender.setDefaultEncoding("UTF-8");
    mailSender.setJavaMailProperties(getMailProperties());

    return mailSender;
  }
}
