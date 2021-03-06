package ru.nukkitx;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.network.protocol.*;
import cn.nukkit.plugin.PluginBase;

public class Dimensions extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDataPacketSend(DataPacketSendEvent event) {
        DataPacket packet = event.getPacket();
        Player player = event.getPlayer();

        if (packet instanceof StartGamePacket) {
            StartGamePacket startGamePacket = (StartGamePacket) packet;
            startGamePacket.dimension = (byte) player.getLevel().getDimension();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
    	if(event.getEntity().getLocation().getLevel().getDimension() != Level.DIMENSION_OVERWORLD) {
    		 Player player = event.getEntity();
    		 Location location = player.getLocation();
    		 Item[] drops = event.getDrops();
    		 int xp = event.getExperience();
    		 location.getLevel().dropExpOrb(location, xp);
    		 for(Item drop : drops) {
    			 location.getLevel().dropItem(location, drop);
    		 }
    		 event.setDrops(new Item[0]);
    		 event.setExperience(0);
    	     player.teleport(player.getSpawn(), TeleportCause.PLUGIN);
    	}       
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLevelChange(EntityLevelChangeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            int fromLevelDimension = event.getOrigin().getDimension();
            int toLevelDimension = event.getTarget().getDimension();

            ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
            changeDimensionPacket.dimension = toLevelDimension;
            changeDimensionPacket.x = (float) player.x;
            changeDimensionPacket.y = (float) player.y;
            changeDimensionPacket.z = (float) player.z;
            changeDimensionPacket.respawn = true;

            PlayStatusPacket playStatusPacket = new PlayStatusPacket();
            playStatusPacket.status = PlayStatusPacket.PLAYER_SPAWN;

            if (fromLevelDimension != toLevelDimension) {
                player.dataPacket(changeDimensionPacket);
                player.dataPacket(playStatusPacket);
            }
        }
    }

    /*
     *
     * Very bad fix, but so far I haven’t come up with another
     *
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.teleport(Server.getInstance().getDefaultLevel().getSpawnLocation());
    }
}