package fr.roytreo.sonarhearing.core.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import fr.roytreo.sonarhearing.core.SonarHearingPlugin;
import fr.roytreo.sonarhearing.core.manager.URLManager;
import fr.roytreo.sonarhearing.core.util.Utils;

/**
 * @author Roytreo28
 */
public class SonarHearingCommand implements CommandExecutor {
	
	private SonarHearingPlugin plugin;
	private final String SONARHEARING_TOGGLE_PERMISSION = "sonar.hearing.toggle";
	private final String SONARHEARING_TOGGLE_OTHER_PERMISSION = "sonar.hearing.toggle.other";
	
	public SonarHearingCommand(SonarHearingPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0)
		{
			if (this.plugin.debuggers.contains(sender))
			{
				sender.sendMessage(ChatColor.RED + "No command allowed during debugging :(");
				return true;
			}
			if (args.length == 1)
			{
				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (args[0].equalsIgnoreCase("on")) {
						if (!player.hasPermission(SONARHEARING_TOGGLE_PERMISSION)) {
							player.sendMessage(ChatColor.RED + "You are not allowed to do that.");
							return true;
						}
						if (this.plugin.disablePlayersAbility.contains(player.getName())) 
							this.plugin.disablePlayersAbility.remove(player.getName());
						player.sendMessage("�7Your sonar hearing ability is now �aenable�7.");
						return true;
					} else if (args[0].equalsIgnoreCase("off")) {
						if (!player.hasPermission(SONARHEARING_TOGGLE_PERMISSION)) {
							player.sendMessage(ChatColor.RED + "You are not allowed to do that.");
							return true;
						}
						if (!this.plugin.disablePlayersAbility.contains(player.getName())) 
							this.plugin.disablePlayersAbility.add(player.getName());
						player.sendMessage("�7Your sonar hearing ability is now �cdisable�7.");
						return true;
					}
					sender.sendMessage(ChatColor.RED + "Invalid command, try /sh.");
					return true;
				} 
				if (args[0].equalsIgnoreCase("update")) {
					if (!sender.isOp()) {
						sender.sendMessage(ChatColor.RED + "You are not allowed to do that.");
						return true;
					}
					if (this.plugin.UPDATE) {
						sender.sendMessage(ChatColor.AQUA + "Stay informed about what the update bring new at https://www.spigotmc.org/resources/sonar-hearing-1-9-1-10-1-11.22640/updates");
						sender.sendMessage(ChatColor.GOLD + "The updating task will start in 10 seconds, then your server will shutdown to complete the updating process.");
						new BukkitRunnable()
						{
							public void run()
							{
								if (URLManager.update(plugin, URLManager.getLatestVersion(), false, URLManager.Link.GITHUB_PATH))
									new BukkitRunnable()
									{
										public void run()
										{
											plugin.deletePluginsJar();
											Bukkit.getServer().shutdown();
										}
									}.runTaskLater(plugin, 100);
							}
						}.runTaskLater(this.plugin, 20*10);
					} else {
						sender.sendMessage(ChatColor.RED + "SonarHearing is already up-to-date.");
					}
					return true;
				}
				sender.sendMessage(ChatColor.RED + "Invalid command, try /sh.");
				return true;
			} else if (args.length == 2) 
			{
				if (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off") && !args[0].equalsIgnoreCase("debug")) {
					sender.sendMessage(ChatColor.RED + "Invalid command, try /sh.");
					return true;
				}
				String playerArg = args[1];
				if (Bukkit.getPlayer(playerArg) != null)
				{
					final Player bukkitPlayer = Bukkit.getPlayer(playerArg);
					if (args[0].equalsIgnoreCase("on")) {
						if (!sender.hasPermission(SONARHEARING_TOGGLE_OTHER_PERMISSION)) {
							sender.sendMessage(ChatColor.RED + "You are not allowed to do that.");
							return true;
						}
						if (this.plugin.disablePlayersAbility.contains(bukkitPlayer.getName())) 
							this.plugin.disablePlayersAbility.remove(bukkitPlayer.getName());
						sender.sendMessage("�7" + bukkitPlayer.getName() + "'s sonar hearing ability is now �aenable�7.");
						return true;
					} else if (args[0].equalsIgnoreCase("off")) {
						if (!sender.hasPermission(SONARHEARING_TOGGLE_OTHER_PERMISSION)) {
							sender.sendMessage(ChatColor.RED + "You are not allowed to do that.");
							return true;
						}
						if (!this.plugin.disablePlayersAbility.contains(bukkitPlayer.getName())) this.plugin.disablePlayersAbility.add(bukkitPlayer.getName());
						sender.sendMessage("�7" + bukkitPlayer.getName() + "'s sonar hearing ability is now �cdisable�7.");
						return true;
					} else if (args[0].equalsIgnoreCase("debug")) {
						if (!sender.isOp() || this.plugin.debuggers.contains(sender) || !(sender instanceof Player)) {
							sender.sendMessage(ChatColor.RED + "You are not allowed to do that." + (!sender.isOp() ? " (Not an Operator)" : "") + (this.plugin.debuggers.contains(sender) ? " (You can't use the debug service twice at the same time)" : "") + (!(sender instanceof Player) ? " (You're not a player)" : ""));
							return true;
						}
						this.plugin.debuggers.add(sender);
						if (!this.plugin.disablePlayersAbility.contains(sender.getName())) this.plugin.disablePlayersAbility.add(sender.getName());
						sender.sendMessage("�a--- Starting a new debugging session ---");
						sender.sendMessage("�7We will try to debug �a" + bukkitPlayer.getName() + "�7's glowing effect ...");
						sender.sendMessage("�7 To do this, we will see if the player is considered as glowing by the server and by you ...");
						new BukkitRunnable()
						{
							int count = 0;
							int serverSide = 0;
							int apiSide = 0;
							public void run()
							{
								if (Utils.isGlowing(bukkitPlayer))
									serverSide++;
								if (GlowAPI.isGlowing(bukkitPlayer, (Player) sender))
									apiSide++;
								count++;
								if (count >= 100)
								{
									cancel();
									sender.sendMessage("�eWe did " + count + " tests:");
									sender.sendMessage("�7- �a" + serverSide + " �7time(s) the server was saying that the player was glowing.");
									sender.sendMessage("�7- �a" + apiSide + " �7time(s) it seems that you were seeing the player as glowing.");
									sender.sendMessage("�eConclusions: ");
									if (Utils.inRange(serverSide, 95, 100))
										sender.sendMessage("�7- We are thinking that the player has recently used a glowing potion.");
									if (Utils.inRange(apiSide, 95, 100))
										sender.sendMessage("�7- It seems that the player is constantly glowing for you.");
									if (Utils.inRange(serverSide, 30, 70))
										sender.sendMessage("�7- Is there a command block which adds potion effects ?");
									if (Utils.inRange(apiSide, 30, 70)) {
										sender.sendMessage("�7- It seems that the player's glowing effect is flickering for you.");
										sender.sendMessage("�7 Let's make a new test! But now, we will switch off the glowing effect of the player and see if you're always seeing it.");
										GlowAPI.setGlowing(bukkitPlayer, false, (Player) sender);
										new BukkitRunnable()
										{
											int count2 = 0;
											int apiSide2 = 0;
											public void run()
											{
												if (GlowAPI.isGlowing(bukkitPlayer, (Player) sender))
													apiSide2++;
												count2++;
												if (count2 >= 100)
												{
													cancel();
													sender.sendMessage("�eWe did " + count2 + " tests:");
													sender.sendMessage("�7- �a" + apiSide2 + " �7time(s) it seems that you were seeing the player as glowing.");
													sender.sendMessage("�eConclusions: ");
													if (Utils.inRange(apiSide2, 30, 100))
														sender.sendMessage("�7- Oh my god! There is a serious bug which prevent the plugin to actually stop the glowing effect of the player.");
													if (Utils.inRange(apiSide2, 0, 5))
														sender.sendMessage("�7- All seems to be good, we were just needing to stop the glowing effect of the player.");
													sender.sendMessage("�7If you were having an issue with a player who was flickering, please take a screen of all the chat during this debugging session and send it to the developer on spigot, thanks.");
												}
											}
										}.runTaskTimer(plugin, 20*6, 1);
									}
									if (Utils.inRange(serverSide, 0, 5))
										sender.sendMessage("�7- The player has probably no glowing potion effect.");
									if (Utils.inRange(apiSide, 0, 5) && !Utils.inRange(serverSide, 95, 100))
										sender.sendMessage("�7- The player seems to be not glowing for you.");
									sender.sendMessage("�c--- End of the debugging session ---");
									if (plugin.disablePlayersAbility.contains(sender.getName())) 
										plugin.disablePlayersAbility.remove(sender.getName());
									plugin.debuggers.remove(sender);
								}
							}
						}.runTaskTimer(this.plugin, 60, 1);
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "This player isn't online.");
					return true;
				}
			}
			sender.sendMessage(ChatColor.RED + "Invalid command, try /sh.");
			return true;
		}
		sender.sendMessage("�a�l�m=============================================");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Sonar hearing is a light weight plugin which allows you to detect entities around you when sneaking. This plugin is perfect for DayZ/LastOfUs/ZombieMania servers.");
        if (sender.isOp()) {
            sender.sendMessage(ChatColor.GRAY + "It seems that your are an Administrator of the server, so you can edit the file configuration of the plugin to suit your needs about mobs detection radius, blocks list, etcetera.");
        }
        sender.sendMessage("");
        if (sender.hasPermission(SONARHEARING_TOGGLE_OTHER_PERMISSION) || sender.hasPermission(SONARHEARING_TOGGLE_PERMISSION)) {
        	sender.sendMessage(String.valueOf(ChatColor.GREEN.toString()) + ChatColor.UNDERLINE + "Commands :");
        	sender.sendMessage("");
        	if (sender.hasPermission(SONARHEARING_TOGGLE_PERMISSION) && sender instanceof Player) {
        		sender.sendMessage(ChatColor.WHITE + "/sh on " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "enable your sonar hearing ability");
        		sender.sendMessage(ChatColor.WHITE + "/sh off " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "disable your sonar hearing ability");
        	}
        	if (sender.hasPermission(SONARHEARING_TOGGLE_OTHER_PERMISSION)) {
        		sender.sendMessage(ChatColor.WHITE + "/sh on <player> " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "enable player's sonar hearing ability");
        		sender.sendMessage(ChatColor.WHITE + "/sh off <player> " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "disable player's sonar hearing ability");
        	}
        	if (sender.isOp())
        		sender.sendMessage(ChatColor.RED + "/sh update " + ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + "update the plugin automatically");
        	sender.sendMessage("");
        }
        sender.sendMessage(String.valueOf(ChatColor.GREEN.toString()) + ChatColor.UNDERLINE + "Special thanks to :");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.WHITE + "    - SQRTdude for this concept");
        sender.sendMessage(ChatColor.WHITE + "    - Asynchronous the developer");
        sender.sendMessage("");
        sender.sendMessage("�a�l�m=============================================");
        return true;
	}
}
