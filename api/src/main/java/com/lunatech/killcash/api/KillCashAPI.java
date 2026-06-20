package com.lunatech.killcash.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * The KillCashAPI class is the main entry point for accessing the KillCash API.
 */
public abstract class KillCashAPI {
    private static KillCashAPI INSTANCE;

    /**
     * Gets the instance of the KillCashAPI.
     *
     * @return the instance of KillCashAPI
     * @since 1.0.0
     */
    public static KillCashAPI getInstance() {
        if (INSTANCE == null)
            throw new RuntimeException("API was accessed before being initialized!");
        return INSTANCE;
    }

    /**
     * Sets the instance of the KillCashAPI.
     * This method is intended for internal use by the api provider only.
     *
     * @param api the instance of KillCashAPI to set
     * @since 1.0.0
     */
    @ApiStatus.Internal
    protected static void setInstance(KillCashAPI api) {
        INSTANCE = api;
    }
}
