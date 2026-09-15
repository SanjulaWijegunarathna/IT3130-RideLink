package com.ridelink.fare.service;

import com.ridelink.fare.dto.PayRequest;
import com.ridelink.fare.dto.PaymentResponse;
import com.ridelink.fare.dto.RideCompletedEvent;
import com.ridelink.fare.exception.ApiException;
import com.ridelink.fare.model.Payment;
import com.ridelink.fare.model.PaymentStatus;
import com.ridelink.fare.queue.InMemoryEventQueue;
import com.ridelink.fare.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InMemoryEventQueue eventQueue;

    public PaymentService(PaymentRepository paymentRepository, InMemoryEventQueue eventQueue) {
        this.paymentRepository = paymentRepository;
        this.eventQueue = eventQueue;
    }

    @Transactional
    public PaymentResponse recordPayment(PayRequest request) {
        if (paymentRepository.existsByRideId(request.getRideId())) {
            throw new ApiException(HttpStatus.CONFLICT, "PAYMENT_EXISTS",
                    "A payment already exists for ride " + request.getRideId());
        }

        boolean simulateFailure = Boolean.TRUE.equals(request.getSimulateFailure());
        BigDecimal amount = request.getAmount();
        if (amount == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AMOUNT_REQUIRED",
                    "amount is required for synchronous payment");
        }

        Payment payment = new Payment();
        payment.setRideId(request.getRideId());
        payment.setPassengerAccountId(request.getPassengerAccountId());
        payment.setAmount(amount);
        payment.setCurrency("LKR");
        payment.setReceiptNumber(generateReceiptNumber());

        if (simulateFailure) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment gateway failure");
            Payment saved = paymentRepository.save(payment);
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED",
                    "Payment failed for ride " + request.getRideId() + " (receipt " + saved.getReceiptNumber() + ")");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getByRideId(Long rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND",
                        "No payment found for ride " + rideId));
        return toResponse(payment);
    }

    public PaymentResponse getByReceipt(String receiptNumber) {
        Payment payment = paymentRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND",
                        "No payment found for receipt " + receiptNumber));
        return toResponse(payment);
    }

    public void enqueueRideCompleted(RideCompletedEvent event) {
        eventQueue.enqueue(event);
    }

    private String generateReceiptNumber() {
        return "RCPT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getPassengerAccountId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getReceiptNumber(),
                payment.getFailureReason(),
                payment.getCreatedAt());
    }
}
