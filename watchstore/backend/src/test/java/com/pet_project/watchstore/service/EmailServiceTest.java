package com.pet_project.watchstore.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Test
    void sendsOtpAndPaymentOutcomeToCustomer() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        EmailService service = new EmailService(provider, "smtp.gmail.com",
                "sender@gmail.com", "app-password", "http://localhost:5173");

        service.sendRegistrationCode("customer@example.com", "123456");
        service.sendPasswordResetCode("customer@example.com", "654321");
        service.sendPaymentResult("customer@example.com", 42, new BigDecimal("125000.00"), true);
        service.sendPaymentResult("customer@example.com", 43, new BigDecimal("125000.00"), false);

        ArgumentCaptor<SimpleMailMessage> messages = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender, org.mockito.Mockito.times(4)).send(messages.capture());
        assertEquals("customer@example.com", messages.getAllValues().get(0).getTo()[0]);
        assertEquals("sender@gmail.com", messages.getAllValues().get(0).getFrom());
        assertTrue(messages.getAllValues().get(0).getText().contains("123456"));
        assertTrue(messages.getAllValues().get(1).getText().contains("654321"));
        assertTrue(messages.getAllValues().get(2).getSubject().contains("thành công"));
        assertTrue(messages.getAllValues().get(3).getSubject().contains("thất bại"));
    }
}
