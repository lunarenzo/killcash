package com.lunatech.killcash;

import com.lunatech.killcash.api.KillCashAPI;

class KillCashAPIProvider extends KillCashAPI implements Reloadable {
    private final KillCash plugin;

    KillCashAPIProvider(KillCash plugin) {
        super();
        this.plugin = plugin;
        setInstance(this);
    }
}
