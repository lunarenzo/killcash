package com.lunatech.killcash.listener.player;

import com.lunatech.killcash.KillCash;
import com.lunatech.killcash.config.PluginConfig;
import com.lunatech.killcash.config.DeathMessagesConfig;
import net.kyori.adventure.text.Component;
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
        DeathMessagesConfig config = plugin.getConfigHandler().getDeathMessagesConfig();
        if (config == null || !config.enabled) {
            return;
        }

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) {
            killer = com.lunatech.killcash.hook.PvPManagerHook.getCombatOpponent(victim);
        }

        Component finalMessage;

        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            finalMessage = handlePvpDeath(victim, killer, lastDamage, config);
        } else {
            if (lastDamage instanceof EntityDamageByEntityEvent entityDamageEvent) {
                org.bukkit.entity.Entity damager = entityDamageEvent.getDamager();
                if (damager instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
                    damager = shooter;
                }

                if (damager instanceof LivingEntity mob && !(mob instanceof Player)) {
                    finalMessage = handleMobDeath(victim, mob, lastDamage, config);
                } else {
                    finalMessage = handleNaturalDeath(victim, lastDamage, config);
                }
            } else {
                finalMessage = handleNaturalDeath(victim, lastDamage, config);
            }
        }

        if (finalMessage != null) {
            event.deathMessage(finalMessage);
        }
    }

    private Component handlePvpDeath(Player victim, Player killer, EntityDamageEvent lastDamage, DeathMessagesConfig config) {
        ItemStack weapon = getWeaponUsed(killer, lastDamage);
        boolean holdingWeapon = weapon != null && weapon.getType() != Material.AIR;

        List<String> templates = holdingWeapon ? config.pvpFormats : config.pvpFistFormats;
        if (templates == null || templates.isEmpty()) {
            return null;
        }

        String template = templates.get(ThreadLocalRandom.current().nextInt(templates.size()));
        int streak = com.lunatech.killcash.pdc.PDCUtil.getInt(killer, com.lunatech.killcash.constant.PDCKeys.STREAK, 0);

        TagResolver resolvers = TagResolver.resolver(
            Placeholder.component("victim", Component.text(victim.getName())),
            Placeholder.component("killer", Component.text(killer.getName())),
            Placeholder.unparsed("streak", String.valueOf(streak))
        );

        if (template.contains("<weapon_type>")) {
            resolvers = TagResolver.resolver(resolvers, Placeholder.parsed("weapon_type", getWeaponType(weapon, config)));
        }

        if (holdingWeapon && template.contains("<item>")) {
            resolvers = TagResolver.resolver(resolvers, Placeholder.component("item", buildHoverableItemComponent(weapon)));
        }

        return MiniMessage.miniMessage().deserialize(template, resolvers);
    }

    private Component handleMobDeath(Player victim, LivingEntity mob, EntityDamageEvent lastDamage, DeathMessagesConfig config) {
        String entityType = mob.getType().name();
        DeathMessagesConfig.MobFormatGroup formatGroup = config.mobFormats.get(entityType);
        if (formatGroup == null) {
            formatGroup = config.mobFormats.get("DEFAULT");
        }
        if (formatGroup == null) {
            return null;
        }

        ItemStack weapon = getWeaponUsed(mob, lastDamage);
        boolean holdingWeapon = weapon != null && weapon.getType() != Material.AIR;

        List<String> templates = holdingWeapon ? formatGroup.weapon : formatGroup.unarmed;
        // Fallback if the configured list is empty
        if (templates == null || templates.isEmpty()) {
            templates = holdingWeapon ? formatGroup.unarmed : formatGroup.weapon;
        }
        // Fallback to DEFAULT format group if still empty
        if (templates == null || templates.isEmpty()) {
            DeathMessagesConfig.MobFormatGroup defaultGroup = config.mobFormats.get("DEFAULT");
            if (defaultGroup != null) {
                templates = holdingWeapon ? defaultGroup.weapon : defaultGroup.unarmed;
                if (templates == null || templates.isEmpty()) {
                    templates = holdingWeapon ? defaultGroup.unarmed : defaultGroup.weapon;
                }
            }
        }

        if (templates == null || templates.isEmpty()) {
            return null;
        }

        String template = templates.get(ThreadLocalRandom.current().nextInt(templates.size()));
        Component mobName = mob.customName() != null ? mob.customName() : Component.translatable(mob.getType().translationKey());

        TagResolver resolvers = TagResolver.resolver(
            Placeholder.component("victim", Component.text(victim.getName())),
            Placeholder.component("killer", mobName)
        );

        if (template.contains("<weapon_type>")) {
            resolvers = TagResolver.resolver(resolvers, Placeholder.parsed("weapon_type", getWeaponType(weapon, config)));
        }

        if (holdingWeapon && template.contains("<item>")) {
            resolvers = TagResolver.resolver(resolvers, Placeholder.component("item", buildHoverableItemComponent(weapon)));
        }

        return MiniMessage.miniMessage().deserialize(template, resolvers);
    }

    private Component handleNaturalDeath(Player victim, EntityDamageEvent lastDamage, DeathMessagesConfig config) {
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

    private ItemStack getWeaponUsed(LivingEntity killer, EntityDamageEvent lastDamage) {
        if (lastDamage instanceof EntityDamageByEntityEvent entityDamageEvent) {
            org.bukkit.entity.Entity damager = entityDamageEvent.getDamager();

            if (damager instanceof org.bukkit.entity.ThrowableProjectile throwableProj) {
                return throwableProj.getItem();
            }
            if (damager instanceof org.bukkit.entity.Trident trident) {
                return trident.getItem();
            }
            if (damager instanceof org.bukkit.entity.Firework firework) {
                return firework.getItem();
            }
            if (damager instanceof Projectile projectile) {
                ItemStack handItem = killer.getEquipment() != null ? killer.getEquipment().getItemInMainHand() : null;
                if (handItem != null && (handItem.getType() == Material.BOW || handItem.getType() == Material.CROSSBOW)) {
                    return handItem;
                }
                if (projectile instanceof org.bukkit.entity.Arrow) {
                    return new ItemStack(Material.ARROW);
                }
                if (projectile instanceof org.bukkit.entity.SpectralArrow) {
                    return new ItemStack(Material.SPECTRAL_ARROW);
                }
            }
        }
        return killer.getEquipment() != null ? killer.getEquipment().getItemInMainHand() : null;
    }

    private String getWeaponType(ItemStack weapon, DeathMessagesConfig config) {
        String materialName = (weapon != null && weapon.getType() != Material.AIR) ? weapon.getType().name() : "AIR";
        String type = config.weaponTypes.get(materialName);
        if (type == null) {
            type = config.weaponTypes.getOrDefault("DEFAULT", "");
        }
        return type;
    }

    private Component buildHoverableItemComponent(ItemStack item) {
        // No manual bracket wrapping here; the server owner can wrap the tag like [<item>] in config.yml
        return item.displayName().hoverEvent(item.asHoverEvent());
    }
}
