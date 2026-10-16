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

/**
 * PaymentService component.
 * PaymentService contains the business logic used by the fare-payment service.
 *
 * This file is part of the RideLink fare-payment service and documents the
 * responsibility of the code defined in this class or helper.
 */
@Service
public class PaymentService {

    // =========================================================================
    // SECTION 1: REPOSITORY & IN-MEMORY QUEUE DEPENDENCIES
    // =========================================================================
    private final PaymentRepository paymentRepository;
    private final InMemoryEventQueue eventQueue;

    public PaymentService(PaymentRepository paymentRepository, InMemoryEventQueue eventQueue) {
        this.paymentRepository = paymentRepository;
        this.eventQueue = eventQueue;
    }

    // =========================================================================
    // SECTION 2: SYNCHRONOUS PAYMENT PROCESSING (With Database Transaction)
    // =========================================================================

    /**
     * Executes synchronous payment processing.
     * Annotated with @Transactional so that if any unexpected error occurs,
     * DB operations are automatically rolled back.
     *
     * Detailed execution steps:
     * - Step 1: Idempotency Check - prevent paying twice for the same ride.
     * - Step 2: Validate amount (cannot be null).
     * - Step 3: Instantiate Payment entity and assign random receipt number.
     * - Step 4: Check simulateFailure flag (used for negative scenario testing).
     * - Step 5: Mark status as SUCCESS and persist record to database.
     */
    @Transactional
    public PaymentResponse recordPayment(PayRequest request) {
        // Step 1: Idempotency check (throws 409 CONFLICT if already exists)
        if (paymentRepository.existsByRideId(request.getRideId())) {
            throw new ApiException(HttpStatus.CONFLICT, "PAYMENT_EXISTS",
                    "A payment already exists for ride " + request.getRideId());
        }

        // Step 2: Validate the payment amount
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

    /**
     * Finds payment record by rideId.
     */
    public PaymentResponse getByRideId(Long rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND",
                        "No payment found for ride " + rideId));
        return toResponse(payment);
    }

    /**
     * Finds payment record by receipt number
     */
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
