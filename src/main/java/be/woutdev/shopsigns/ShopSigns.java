package be.woutdev.shopsigns;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.shopsigns.listener.PlayerInteractListener;
import be.woutdev.shopsigns.listener.SignChangeListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Wout on 13/08/2017.
 */
public class ShopSigns extends JavaPlugin {
    @Override
    public void onEnable() {
        if (EconomyAPI.getAPI() == null)
        {
            Bukkit.getLogger().severe("No Economy API implementation was set! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new SignChangeListener(), this);
    }
}
