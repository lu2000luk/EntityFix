package me.lu2000luk.EntityFix;

import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.logging.Logger;

public class EntityFix extends JavaPlugin implements Listener {
	private Logger logger;

	@Override
	public void onEnable() {
		logger = getLogger();
		getServer().getPluginManager().registerEvents(this, this);
		logger.info("EntityFix plugin enabled");
	}

	public void onEntitySpawn(EntitySpawnEvent event) {
		if (event.getEntity() instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) event.getEntity();
			EntityEquipment equipment = entity.getEquipment();
			if (equipment != null) {
				logger.info("Checking equipment of spawned entity: " + entity.getType());
				for (ItemStack item : equipment.getArmorContents()) {
					if (item != null) {
						removeZeroLevelEnchantments(item, "armor");
					}
				}
				removeZeroLevelEnchantments(equipment.getItemInMainHand(), "main hand");
				removeZeroLevelEnchantments(equipment.getItemInOffHand(), "off hand");
			}
		}
	}

	@EventHandler
	public void onEntityPickupItem(EntityPickupItemEvent event) {
		logger.info("Entity " + event.getEntity().getType() + " picked up item");
		removeZeroLevelEnchantments(event.getItem().getItemStack(), "picked up item");
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
			logger.info("Inventory click in Creative mode by " + event.getWhoClicked().getName());
			if (event.getCurrentItem() != null) {
				removeZeroLevelEnchantments(event.getCurrentItem(), "current item");
			}
			if (event.getCursor() != null) {
				removeZeroLevelEnchantments(event.getCursor(), "cursor item");
			}
		}
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		String[] args = event.getMessage().split(" ");
		if (args.length >= 2 && args[0].equalsIgnoreCase("/give")) {
			logger.info("Give command used by " + event.getPlayer().getName());
			getServer().getScheduler().runTaskLater(this, () -> {
				for (ItemStack item : event.getPlayer().getInventory().getContents()) {
					if (item != null) {
						removeZeroLevelEnchantments(item, "given item");
					}
				}
			}, 1L);
		}
	}

	@EventHandler
	public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
		logger.info("Player " + event.getPlayer().getName() + " swapped hand items");
		removeZeroLevelEnchantments(event.getMainHandItem(), "main hand (swap)");
		removeZeroLevelEnchantments(event.getOffHandItem(), "off hand (swap)");
	}

	private void removeZeroLevelEnchantments(ItemStack item, String itemDescription) {
		if (item != null && item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
			logger.info("Checking enchantments on " + itemDescription + ": " + item.getType());
			boolean changed = false;
			org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
			if (meta != null) {
				for (Enchantment enchantment : new ArrayList<>(meta.getEnchants().keySet())) {
					if (meta.getEnchantLevel(enchantment) == 0) {
						logger.info("Removing zero-level enchantment: " + enchantment.getKey().getKey());
						meta.removeEnchant(enchantment);
						changed = true;
					}
				}
				if (changed) {
					item.setItemMeta(meta);
					logger.info("Updated item meta after removing enchantments");
				}
			}
		}
	}
}