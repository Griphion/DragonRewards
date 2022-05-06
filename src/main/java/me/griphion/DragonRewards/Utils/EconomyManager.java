package me.griphion.DragonRewards.Utils;

import me.griphion.DragonRewards.Core;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final Core plugin;
    public Economy eco;

    public EconomyManager(Core plugin){
        this.plugin = plugin;
    }

    public boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economy = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economy != null)
            eco = economy.getProvider();
        return (eco != null);
    }
}
