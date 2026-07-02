package com.lunatech.killcash.listener.player;

import com.lunatech.killcash.KillCash;
import com.lunatech.killcash.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener implementing custom death messages with dynamic placeholders and native hoverable items.
 */
public final class DeathMessageListener implements Listener {
    private final KillCash plugin;

    public DeathMessageListener(KillCash plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PluginConfig config = plugin.getConfigHandler().getConfig();
        if (config == null || config.deathMessages == null || !config.deathMessages.enabled) {
            return;
        }

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        Component finalMessage;

        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            finalMessage = handlePvpDeath(victim, killer, config.deathMessages);
        } else {
            EntityDamageEvent lastDamage = victim.getLastDamageCause();
            if (lastDamage instanceof EntityDamageByEntityEvent entityDamageEvent) {
                org.bukkit.entity.Entity damager = entityDamageEvent.getDamager();
                if (damager instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
                    damager = shooter;
                }

                if (damager instanceof LivingEntity mob && !(mob instanceof Player)) {
                    finalMessage = handleMobDeath(victim, mob, config.deathMessages);
                } else {
                    finalMessage = handleNaturalDeath(victim, lastDamage, config.deathMessages);
                }
            } else {
                finalMessage = handleNaturalDeath(victim, lastDamage, config.deathMessages);
            }
        }

        if (finalMessage != null) {
            event.deathMessage(finalMessage);
        }
    }

    private Component handlePvpDeath(Player victim, Player killer, PluginConfig.DeathMessages config) {
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        boolean holdingWeapon = weapon != null && weapon.getType() != Material.AIR;

        List<String> templates = holdingWeapon ? config.pvpFormats : config.pvpFistFormats;
        if (templates == null || templates.isEmpty()) {
            return null;
        }

        String template = templates.get(ThreadLocalRandom.current().nextInt(templates.size()));
        int streak = com.lunatech.killcash.pdc.PDCUtil.getInt(killer, com.lunatech.killcash.constant.PDCKeys.STREAK, 0);

        TagResolver baseResolvers = TagResolver.resolver(
            Placeholder.component("victim", Component.text(victim.getName())),
            Placeholder.component("killer", Component.text(killer.getName())),
            Placeholder.unparsed("streak", String.valueOf(streak))
        );

        if (holdingWeapon && template.contains("<item>")) {
            Component itemComponent = buildHoverableItemComponent(weapon);
            TagResolver pvpResolvers = TagResolver.resolver(
                baseResolvers,
                Placeholder.component("item", itemComponent)
            );
            return MiniMessage.miniMessage().deserialize(template, pvpResolvers);
        }

        return MiniMessage.miniMessage().deserialize(template, baseResolvers);
    }

    private Component handleMobDeath(Player victim, LivingEntity mob, PluginConfig.DeathMessages config) {
        List<String> templates = config.mobFormats;
        if (templates == null || templates.isEmpty()) {
            return null;
        }

        String template = templates.get(ThreadLocalRandom.current().nextInt(templates.size()));

        EntityEquipment equipment = mob.getEquipment();
        ItemStack weapon = equipment != null ? equipment.getItemInMainHand() : null;
        boolean holdingWeapon = weapon != null && weapon.getType() != Material.AIR;

        Component mobName = mob.customName() != null ? mob.customName() : Component.translatable(mob.getType().translationKey());

        TagResolver baseResolvers = TagResolver.resolver(
            Placeholder.component("victim", Component.text(victim.getName())),
            Placeholder.component("killer", mobName)
        );

        if (holdingWeapon && template.contains("<item>")) {
            Component itemComponent = buildHoverableItemComponent(weapon);
            TagResolver resolvers = TagResolver.resolver(
                baseResolvers,
                Placeholder.component("item", itemComponent)
            );
            return MiniMessage.miniMessage().deserialize(template, resolvers);
        }

        return MiniMessage.miniMessage().deserialize(template, baseResolvers);
    }

    private Component handleNaturalDeath(Player victim, EntityDamageEvent lastDamage, PluginConfig.DeathMessages config) {
        EntityDamageEvent.DamageCause cause = (lastDamage != null) ? lastDamage.getCause() : EntityDamageEvent.DamageCause.CUSTOM;
        String causeName = cause.name();

        String template = config.naturalFormats.get(causeName);
        if (template == null) {
            template = config.naturalFormats.getOrDefault("DEFAULT", "<red><victim> <gray>died.");
        }

        return MiniMessage.miniMessage().deserialize(
            template,
            Placeholder.component("victim", Component.text(victim.getName()))
        );
    }

    private Component buildHoverableItemComponent(ItemStack item) {
        Component displayName = item.displayName();
        Component framedDisplay = Component.empty()
            .append(Component.text("["))
            .append(displayName)
            .append(Component.text("]"));

        return framedDisplay.hoverEvent(item.asHoverEvent());
    }
}
