package com.ridelink.fare.queue;

import com.ridelink.fare.dto.RideCompletedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * In-memory stand-in for an async message broker (e.g. RabbitMQ/Kafka).
 * Events are enqueued by the API and drained by {@link PaymentEventWorker}.
 */
@Component
public class InMemoryEventQueue {

    private final ConcurrentLinkedQueue<RideCompletedEvent> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(RideCompletedEvent event) {
        queue.offer(event);
    }

    public RideCompletedEvent poll() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }
}
