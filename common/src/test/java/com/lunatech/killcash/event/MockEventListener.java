package com.lunatech.killcash.event;

@FunctionalInterface
public interface MockEventListener {
    void onEvent(MockEvent event);
}