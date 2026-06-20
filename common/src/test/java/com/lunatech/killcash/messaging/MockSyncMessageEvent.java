package com.lunatech.killcash.messaging;

import com.lunatech.killcash.event.MockEvent;
import com.lunatech.killcash.messaging.message.Message;

public class MockSyncMessageEvent extends MockEvent {
    private final Message<?> message;

    public MockSyncMessageEvent(Message<?> message) {
        this.message = message;
    }

    public Message<?> getMessage() {
        return message;
    }
}