package fr.roytreo.sonarhearing.v1_9_R2;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.roytreo.sonarhearing.core.version.INMS;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_9_R2.WorldBorder;

/**
 * @author Roytreo28
 */
public class NMS implements INMS {

	@Override
	public void redAlert(Player player, Boolean bool) {
		if (bool) {
			WorldBorder w = new WorldBorder();
			w.setSize(1.0D);
			w.setCenter(player.getLocation().getX() + 10000.0D, player.getLocation().getZ() + 10000.0D);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(
					new PacketPlayOutWorldBorder(w, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
		} else {
			WorldBorder ww = new WorldBorder();
			ww.setSize(3.0E7D);
			ww.setCenter(player.getLocation().getX(), player.getLocation().getZ());
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(
					new PacketPlayOutWorldBorder(ww, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
		}
	}
}
