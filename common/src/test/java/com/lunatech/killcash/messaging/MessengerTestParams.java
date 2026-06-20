package com.lunatech.killcash.messaging;

import com.lunatech.killcash.messaging.broker.BrokerType;

@SuppressWarnings("unused")
public record MessengerTestParams(BrokerType type) {
    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private BrokerType type;

        private Builder() {
        }

        public Builder withType(BrokerType type) {
            this.type = type;
            return this;
        }

        public MessengerTestParams build() {
            return new MessengerTestParams(type);
        }
    }
}
