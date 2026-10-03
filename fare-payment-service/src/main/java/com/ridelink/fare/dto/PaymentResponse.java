package com.ridelink.fare.dto;

import com.ridelink.fare.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {

    private Long id;
    private Long rideId;
    private Long passengerAccountId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String receiptNumber;
    private String failureReason;
    private Instant createdAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id, Long rideId, Long passengerAccountId, BigDecimal amount,
                           String currency, PaymentStatus status, String receiptNumber,
                           String failureReason, Instant createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.passengerAccountId = passengerAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.receiptNumber = receiptNumber;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Long getPassengerAccountId() {
        return passengerAccountId;
    }

    public void setPassengerAccountId(Long passengerAccountId) {
        this.passengerAccountId = passengerAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
