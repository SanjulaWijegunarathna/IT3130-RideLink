package com.ridelink.fare.queue;

import com.ridelink.fare.dto.FareEstimateRequest;
import com.ridelink.fare.dto.RideCompletedEvent;
import com.ridelink.fare.model.Payment;
import com.ridelink.fare.model.PaymentStatus;
import com.ridelink.fare.repository.PaymentRepository;
import com.ridelink.fare.service.FareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Background worker that simulates async payment processing after a ride completes.
 */
@Component
public class PaymentEventWorker {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventWorker.class);

    private final InMemoryEventQueue eventQueue;
    private final PaymentRepository paymentRepository;
    private final FareService fareService;

    public PaymentEventWorker(InMemoryEventQueue eventQueue,
                              PaymentRepository paymentRepository,
                              FareService fareService) {
        this.eventQueue = eventQueue;
        this.paymentRepository = paymentRepository;
        this.fareService = fareService;
    }

    @Scheduled(fixedDelay = 2000)
    public void processRideCompletedEvents() {
        RideCompletedEvent event;
        while ((event = eventQueue.poll()) != null) {
            processEvent(event);
        }
    }

    private void processEvent(RideCompletedEvent event) {
        if (paymentRepository.existsByRideId(event.getRideId())) {
            log.debug("Payment already exists for ride {}", event.getRideId());
            return;
        }

        FareEstimateRequest fareRequest = new FareEstimateRequest();
        fareRequest.setPickup(event.getPickup());
        fareRequest.setDestination(event.getDestination());
        fareRequest.setPickupLat(event.getPickupLat());
        fareRequest.setPickupLng(event.getPickupLng());
        fareRequest.setDestLat(event.getDestLat());
        fareRequest.setDestLng(event.getDestLng());

        BigDecimal amount = fareService.estimate(fareRequest).getEstimatedFare();

        Payment payment = new Payment();
        payment.setRideId(event.getRideId());
        payment.setPassengerAccountId(event.getPassengerAccountId());
        payment.setAmount(amount);
        payment.setCurrency("LKR");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setReceiptNumber(generateReceiptNumber());

        paymentRepository.save(payment);
        log.info("Async payment recorded for ride {} amount {} LKR", event.getRideId(), amount);
    }

    private String generateReceiptNumber() {
        return "RCPT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
