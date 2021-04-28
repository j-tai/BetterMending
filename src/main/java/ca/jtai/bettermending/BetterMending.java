package ca.jtai.bettermending;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BetterMending extends JavaPlugin implements Listener {
    private Path storePath;
    private Store store;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        storePath = getDataFolder().toPath().resolve("store.json");
        store = Store.read(storePath);
        store.write(storePath);
        getServer().getScheduler().runTaskTimer(
            this, () -> store.write(storePath),
            5 * 60 * 20, 5 * 60 * 20
        );
        getCommand("mending").setExecutor(new MendingCmd(store));
    }

    @Override
    public void onDisable() {
        store.write(storePath);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemMend(PlayerItemMendEvent e) {
        switch (store.getMode(e.getPlayer())) {
            case OFF:
            case REPAIR:
                e.setCancelled(true);
                break;
            case VANILLA:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerExpChange(PlayerExpChangeEvent e) {
        switch (store.getMode(e.getPlayer())) {
            case OFF:
            case VANILLA:
                break;
            case REPAIR:
                handleRepairMode(e);
                break;
        }
    }

    private void handleRepairMode(PlayerExpChangeEvent e) {
        int amount = e.getAmount() * 2;
        if (amount <= 0)
            return;
        PlayerInventory inventory = e.getPlayer().getInventory();
        ArrayList<ItemStack> mendableItems = Stream.concat(
            Stream.of(inventory.getArmorContents()),
            Stream.of(inventory.getItemInMainHand(), inventory.getItemInOffHand())
        )
            .filter(Objects::nonNull)
            .filter(item -> item.getEnchantmentLevel(Enchantment.MENDING) != 0)
            .filter(item -> {
                ItemMeta meta = item.getItemMeta();
                return meta instanceof Damageable && ((Damageable) meta).hasDamage();
            })
            .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(mendableItems);
        for (ItemStack item : mendableItems) {
            Damageable meta = (Damageable) item.getItemMeta();
            int damage = meta.getDamage();
            if (damage < amount) {
                // Repair fully
                meta.setDamage(0);
                amount -= damage;
            } else {
                // Repair using the rest of the exp
                meta.setDamage(damage - amount);
                amount = 0;
            }
            item.setItemMeta((ItemMeta) meta);
            if (amount <= 0)
                break;
        }
        e.setAmount((amount + 1) / 2);
    }
}
