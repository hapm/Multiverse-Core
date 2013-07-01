/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.commands;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.utils.webpaste.BitlyURLShortener;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteFailedException;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteService;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceFactory;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceType;
import com.onarandombox.MultiverseCore.utils.webpaste.URLShortener;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * Dumps version info to the console.
 */
public class VersionCommand extends MultiverseCommand {
    private static final URLShortener SHORTENER = new BitlyURLShortener();

    public VersionCommand(MultiverseCore plugin) {
        super(plugin);
        this.setName("Multiverse Version");
        this.setCommandUsage("/mv version " + ChatColor.GOLD + "-[pb]");
        this.setArgRange(0, 1);
        this.addKey("mv version");
        this.addKey("mvv");
        this.addKey("mvversion");
        this.setPermission("multiverse.core.version",
                "Dumps version info to the console, optionally to pastie.org with -p or pastebin.com with a -b.", PermissionDefault.TRUE);
    }

    @Override
    public void runCommand(final CommandSender sender, final List<String> args) {
        // Check if the command was sent from a Player.
        if (sender instanceof Player) {
            sender.sendMessage("Version info dumped to console. Please check your server logs.");
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("[Multiverse-Core] Multiverse-Core Version: ").append(this.plugin.getDescription().getVersion()).append('\n');
        buffer.append("[Multiverse-Core] Bukkit Version: ").append(this.plugin.getServer().getVersion()).append('\n');
        buffer.append("[Multiverse-Core] Loaded Worlds: ").append(this.plugin.getMVWorldManager().getMVWorlds()).append('\n');
        buffer.append("[Multiverse-Core] Multiverse Plugins Loaded: ").append(this.plugin.getPluginCount()).append('\n');
        final boolean usingVault = plugin.getVaultHandler().getEconomy() != null;
        buffer.append("[Multiverse-Core] Using Vault: ").append(usingVault).append('\n');
        if (usingVault) {
            buffer.append("[Multiverse-Core] Economy being used: ").append(plugin.getVaultHandler().getEconomy().getName()).append('\n');
        } else {
            buffer.append("[Multiverse-Core] Economy being used: ").append(this.plugin.getBank().getEconUsed()).append('\n');
        }
        buffer.append("[Multiverse-Core] Permissions Plugin: ").append(this.plugin.getMVPerms().getType()).append('\n');
        buffer.append("[Multiverse-Core] Dumping Config Values: (version ")
                .append(this.plugin.getMVConfig().getVersion()).append(")").append('\n');
        buffer.append("[Multiverse-Core]  messagecooldown: ").append(plugin.getMessaging().getCooldown()).append('\n');
        buffer.append("[Multiverse-Core]  teleportcooldown: ").append(plugin.getMVConfig().getTeleportCooldown()).append('\n');
        buffer.append("[Multiverse-Core]  worldnameprefix: ").append(plugin.getMVConfig().getPrefixChat()).append('\n');
        buffer.append("[Multiverse-Core]  worldnameprefixFormat: ").append(plugin.getMVConfig().getPrefixChatFormat()).append('\n');
        buffer.append("[Multiverse-Core]  enforceaccess: ").append(plugin.getMVConfig().getEnforceAccess()).append('\n');
        buffer.append("[Multiverse-Core]  displaypermerrors: ").append(plugin.getMVConfig().getDisplayPermErrors()).append('\n');
        buffer.append("[Multiverse-Core]  teleportintercept: ").append(plugin.getMVConfig().getTeleportIntercept()).append('\n');
        buffer.append("[Multiverse-Core]  firstspawnoverride: ").append(plugin.getMVConfig().getFirstSpawnOverride()).append('\n');
        buffer.append("[Multiverse-Core]  firstspawnworld: ").append(plugin.getMVConfig().getFirstSpawnWorld()).append('\n');
        buffer.append("[Multiverse-Core]  debug: ").append(plugin.getMVConfig().getGlobalDebug()).append('\n');
        buffer.append("[Multiverse-Core] Special Code: FRN002").append('\n');

        MVVersionEvent versionEvent = new MVVersionEvent(buffer.toString());
        this.plugin.getServer().getPluginManager().callEvent(versionEvent);

        // log to console
        final String data = versionEvent.getVersionInfo();
        String[] lines = data.split("\n");
        for (String line : lines) {
            Logging.info(line);
        }

        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (args.size() == 1) {
                    String pasteUrl;
                    if (args.get(0).equalsIgnoreCase("-p")) {
                        pasteUrl = postToService(PasteServiceType.PASTIE, true, data); // private post to pastie
                    } else if (args.get(0).equalsIgnoreCase("-b")) {
                        pasteUrl = postToService(PasteServiceType.PASTEBIN, true, data); // private post to pastie
                    } else {
                        return;
                    }

                    sender.sendMessage("Version info dumped here: " + ChatColor.GREEN + pasteUrl);
                    Logging.info("Version info dumped here: %s", pasteUrl);
                }
            }
        });
    }

    /**
     * Send the current contents of this.pasteBinBuffer to a web service.
     *
     * @param type      Service type to send to
     * @param isPrivate Should the paste be marked as private.
     * @return URL of visible paste
     */
    private static String postToService(PasteServiceType type, boolean isPrivate, String pasteData) {
        PasteService ps = PasteServiceFactory.getService(type, isPrivate);
        try {
            return SHORTENER.shorten(ps.postData(ps.encodeData(pasteData), ps.getPostURL()));
        } catch (PasteFailedException e) {
            System.out.print(e);
            return "Error posting to service";
        }
    }
}
