package remagdbr.org.mine.relicMemory;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class RelicMemory extends JavaPlugin implements Listener {

    private final Map<String, Location> relicLocations = new HashMap<>();
    private final Set<UUID> activatedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("RelicMemoryPlugin ativado!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (label.equalsIgnoreCase("setmemory") && args.length == 1) {
            String memoryId = args[0];
            relicLocations.put(memoryId, player.getLocation());
            player.sendMessage("§aLocal da memória '" + memoryId + "' definido com sucesso!");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (activatedPlayers.contains(player.getUniqueId())) return;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    String displayName = ChatColor.stripColor(meta.getDisplayName());
                    Location memoryLoc = relicLocations.get(displayName);

                    if (memoryLoc != null && player.getWorld().equals(memoryLoc.getWorld()) && player.getLocation().distance(memoryLoc) < 3) {
                        // Remover do inventário e colocar na mão
                        int slot = player.getInventory().first(item);
                        player.getInventory().setItem(slot, null);
                        player.getInventory().setItemInMainHand(item);

                        // Marcar como ativado
                        activatedPlayers.add(player.getUniqueId());

                        // Efeitos visuais
                        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 100, 0.5, 1, 0.5, 0.01);

                        // Tocar som custom
                        player.playSound(player.getLocation(), "custom.memory_voice_1", 1.0f, 1.0f);

                        // Enviar mensagens com delay
                        player.sendMessage("§7Você sente um frio subindo pela espinha...");
                        Bukkit.getScheduler().runTaskLater(this, () -> player.sendMessage("§f\"Ele ainda está aqui... esperando...\""), 60L);

                        // Buff simbólico
                        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 60, 0));

                        // Mensagem na tela
                        player.sendTitle("§b[Memória Ativada]", "§f\"As sombras daquele dia ainda sussurram...\"", 10, 60, 10);

                        // NPC fantasma simulado (mensagem apenas)
                        Bukkit.getScheduler().runTaskLater(this, () -> player.sendMessage("§8Um vulto aparece por um instante... e some sem dizer nada."), 100L);

                        return;
                    }
                }
            }
        }
    }
}