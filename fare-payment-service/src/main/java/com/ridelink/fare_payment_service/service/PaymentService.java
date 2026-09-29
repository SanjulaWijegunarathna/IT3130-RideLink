package com.ridelink.fare_payment_service.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.entity.PaymentMethod;
import com.ridelink.fare_payment_service.entity.PaymentStatus;
import com.ridelink.fare_payment_service.repository.FarePaymentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final FarePaymentRepository paymentRepository;

    public PaymentService(FarePaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment create(PaymentRequest request) {
        return paymentRepository.findByRideId(request.rideId())
                .orElseGet(() -> savePayment(request));
    }

    public Payment findByRideId(Long rideId) {
        return paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found for ride " + rideId));
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public Payment findById(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found with id " + id));
    }

    public void delete(String id) {
        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException("No payment found with id " + id);
        }
        paymentRepository.deleteById(id);
    }

    @Async
    public void processRideCompleted(PaymentRequest request) {
        create(request);
    }

    private Payment savePayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setRideId(request.rideId());
        payment.setPassengerAccountId(request.passengerAccountId());
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod() == null ? PaymentMethod.CASH : request.paymentMethod());
        payment.setStatus(Boolean.TRUE.equals(request.simulateFailure()) ? PaymentStatus.FAILED : PaymentStatus.COMPLETED);
        payment.setReceiptNumber("RL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setCreatedAt(Instant.now());
        return paymentRepository.save(payment);
    }
}