package com.ridelink.fare.service;

import com.ridelink.fare.dto.PayRequest;
import com.ridelink.fare.dto.PaymentResponse;
import com.ridelink.fare.exception.ApiException;
import com.ridelink.fare.model.Payment;
import com.ridelink.fare.model.PaymentStatus;
import com.ridelink.fare.queue.InMemoryEventQueue;
import com.ridelink.fare.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private InMemoryEventQueue eventQueue;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        eventQueue = new InMemoryEventQueue();
        paymentService = new PaymentService(paymentRepository, eventQueue);
    }

    @Test
    void recordPaymentSuccess() {
        when(paymentRepository.existsByRideId(1L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(10L);
            return payment;
        });

        PayRequest request = new PayRequest();
        request.setRideId(1L);
        request.setPassengerAccountId(99L);
        request.setAmount(new BigDecimal("500.00"));

        PaymentResponse response = paymentService.recordPayment(request);

        assertEquals(1L, response.getRideId());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertTrue(response.getReceiptNumber().startsWith("RCPT-"));
    }

    @Test
    void recordPaymentSimulateFailurePersistsFailedPaymentAndThrows402() {
        when(paymentRepository.existsByRideId(2L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(11L);
            return payment;
        });

        PayRequest request = new PayRequest();
        request.setRideId(2L);
        request.setPassengerAccountId(88L);
        request.setAmount(new BigDecimal("300.00"));
        request.setSimulateFailure(true);

        ApiException ex = assertThrows(ApiException.class, () -> paymentService.recordPayment(request));

        assertEquals(HttpStatus.PAYMENT_REQUIRED, ex.getStatus());
        assertEquals("PAYMENT_FAILED", ex.getError());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.FAILED, captor.getValue().getStatus());
        assertEquals("Simulated payment gateway failure", captor.getValue().getFailureReason());
    }

    @Test
    void recordPaymentDuplicateRideThrows409() {
        when(paymentRepository.existsByRideId(3L)).thenReturn(true);

        PayRequest request = new PayRequest();
        request.setRideId(3L);
        request.setPassengerAccountId(77L);
        request.setAmount(new BigDecimal("200.00"));

        ApiException ex = assertThrows(ApiException.class, () -> paymentService.recordPayment(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("PAYMENT_EXISTS", ex.getError());
    }

    @Test
    void getByRideIdReturnsPayment() {
        Payment payment = new Payment();
        payment.setId(5L);
        payment.setRideId(4L);
        payment.setPassengerAccountId(66L);
        payment.setAmount(new BigDecimal("150.00"));
        payment.setCurrency("LKR");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setReceiptNumber("RCPT-TEST123");

        when(paymentRepository.findByRideId(4L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getByRideId(4L);

        assertEquals(4L, response.getRideId());
        assertEquals("RCPT-TEST123", response.getReceiptNumber());
    }
}
