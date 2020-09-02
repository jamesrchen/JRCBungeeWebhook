package ninja.jrc.jrcbungeewebhook

import com.google.common.io.ByteStreams
import khttp.post
import kr.entree.spigradle.annotations.PluginMain
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import net.md_5.bungee.event.EventHandler
import java.io.File
import java.io.FileOutputStream

@PluginMain
class JRCBungeeWebhook: Plugin() {
    override fun onEnable() {
        logger.info("Initializing")

        // Trying this from https://www.spigotmc.org/threads/bungeecords-configuration-api.11214/, tell me if there is a better method
        if(!dataFolder.exists()) dataFolder.mkdir()
        val configFile = File(dataFolder, "config.yml")
        if(!configFile.exists()){
            configFile.createNewFile()
            val inputStream = getResourceAsStream("config.yml")
            val outputSteam = FileOutputStream(configFile)
            ByteStreams.copy(inputStream, outputSteam)
        }
        val config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(configFile)

        val infoWebhook = config.getString("info-webhook")
        val logWebhook = config.getString("log-webhook")

        logger.info("Info: $infoWebhook")
        logger.info("Logging: $logWebhook")
        val discordManager = DiscordManager(infoWebhook, logWebhook)
        val proxyServer =  ProxyServer.getInstance()
        proxyServer.pluginManager.registerListener(this, EventListener(this, discordManager))
    }
}

class EventListener(val plugin: JRCBungeeWebhook, val discordManager: DiscordManager): Listener {
    @EventHandler
    fun onPlayerJoin(event: LoginEvent) {
        val name = event.connection.name
        plugin.logger.info("$name Joined - Sending to discord")
        discordManager.sendInfo("$name Joined")

        val address = event.connection.socketAddress
        discordManager.sendLog("[$address] $name Joined")
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerDisconnectEvent){
        val name = event.player.name
        plugin.logger.info("$name Left - Sending to discord")
        discordManager.sendInfo("$name Left")

        val address = event.player.socketAddress
        discordManager.sendLog("[$address] $name Left")
    }

    @EventHandler
    fun onPlayerChat(event: ChatEvent){
        val address = event.sender.socketAddress
        var name = "???"
        for (player in ProxyServer.getInstance().players) {
            if(player.socketAddress == address){
                name = player.name
            }
        }
        val message = event.message.replace("`", "\\`")

        if(!event.isCommand){
            discordManager.sendLog("[$address] $name: $message")
        }else{
            discordManager.sendLog("[$address] $name: /[Redacted]")
        }
    }

    @EventHandler
    fun onServerSwitch(event: ServerSwitchEvent) {
        val address = event.player.socketAddress
        val name = event.player.name
        val from = event.from.name
        val to = event.player.server.info.name

        discordManager.sendInfo("$name has moved to the $to server")
        discordManager.sendLog("[$address] $name switched servers from **$from** to **$to**")
    }
}

class DiscordManager(val infoWebhook: String, val logWebhook: String) {
    fun sendInfo(message: String) {
        post(
            infoWebhook,
            mapOf("Content-Type" to "application/json"),
            json = mapOf("content" to message)
        )
    }

    fun sendLog(message: String) {
        post(
            logWebhook,
            mapOf("Content-Type" to "application/json"),
            json = mapOf("content" to message)
        )
    }
}