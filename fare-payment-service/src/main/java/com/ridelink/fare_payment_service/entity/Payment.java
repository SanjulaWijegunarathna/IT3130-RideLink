package com.ridelink.fare_payment_service.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;
    private Long rideId;
    private Long passengerAccountId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String receiptNumber;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public Long getRideId() {
        return rideId;
    }

    public Long getPassengerAccountId() {
        return passengerAccountId;
    }

    public Double getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public void setPassengerAccountId(Long passengerAccountId) {
        this.passengerAccountId = passengerAccountId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}