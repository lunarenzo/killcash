package com.lunatech.killcash.messaging;

import com.lunatech.killcash.event.MockEventSystem;
import com.lunatech.killcash.messaging.adapter.receiver.ReceiverAdapter;
import com.lunatech.killcash.messaging.message.Message;

public class MockReceiverAdapter extends ReceiverAdapter {
    @Override
    public void accept(Message<?> message) {
        MockEventSystem.fireEvent(new MockSyncMessageEvent(message));
    }
}
