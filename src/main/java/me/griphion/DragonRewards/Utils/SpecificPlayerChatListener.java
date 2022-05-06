package me.griphion.DragonRewards.Utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;


/**
 * Escucha los mensajes de un usuario en específico
 *
 * */
public class SpecificPlayerChatListener implements Listener {

    private final Player player;
    private final ActionHandler handler;

    public SpecificPlayerChatListener(Plugin plugin, Player player, ActionHandler handler){
        this.player = player;
        this.handler = handler;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void stop(){
        HandlerList.unregisterAll(this);
    }


    @EventHandler
    public void chatMessageEvent(AsyncPlayerChatEvent event){
        if(event.getPlayer().equals(player)){
            event.setCancelled(true);
            SpecificPlayerChatEvent e = new SpecificPlayerChatEvent(event.getMessage());
            handler.onChatMessageAction(e);
            if(e.willStop()){
                stop();
            }
        }
    }

    public interface ActionHandler {
        void onChatMessageAction(SpecificPlayerChatEvent event);
    }

    public class SpecificPlayerChatEvent{
        private boolean stop = false;
        private final String message;

        public SpecificPlayerChatEvent(String message){
            this.message = message;
        }
        public void setWillStop(boolean stop) {
            this.stop = stop;
        }
        public boolean willStop() {
            return stop;
        }
        public String getMessage(){
            return message;
        }
    }
}
