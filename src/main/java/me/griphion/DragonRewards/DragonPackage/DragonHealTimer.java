package me.griphion.DragonRewards.DragonPackage;

import me.griphion.DragonRewards.Core;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonHealTimer {

    private final Core plugin;
    private final Dragon dragon;
    private long time;
    private final long baseTime;
    private boolean stop = false;

    public DragonHealTimer(Core plugin, Dragon dragon, long time){
        this.plugin = plugin;
        this.dragon = dragon;
        this.time = time;
        baseTime = time;
        start();
    }

    private void start() {
        new BukkitRunnable(){
            @Override
            public void run() {
                if(stop){
                    dragon.stopHealTimer();
                    cancel();
                    return;
                }
                time--;
                if (time <= 0 && dragon != null){
                    dragon.curarYReiniciarAsistencias();
                    reiniciar();
                }
            }
        }.runTaskTimer(plugin,0,20L);
    }

    public void stop(){
        stop = true;
    }

    public void reiniciar(){
        time = baseTime;
    }
}
