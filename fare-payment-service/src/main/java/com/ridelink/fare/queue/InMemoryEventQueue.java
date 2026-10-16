package com.ridelink.fare.queue;

import com.ridelink.fare.dto.RideCompletedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InMemoryEventQueue {

    // Thread-safe FIFO queue storing events in memory without external message
    // broker
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
