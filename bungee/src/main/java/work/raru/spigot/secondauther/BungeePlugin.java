package work.raru.spigot.secondauther;

import lombok.Getter;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.packet.Handshake;
import work.raru.spigot.secondauther.HandshakeHandler.HandshakeResult;
import work.raru.spigot.secondauther.HandshakeHandler.ResultType;
import work.raru.spigot.secondauther.util.ReflectionUtil;

import java.lang.reflect.Field;

public class BungeePlugin extends Plugin implements Listener {
	@Getter
	private static BungeePlugin instance;
	private static Field handshake;

	@Getter
	private FloodgateConfig config;
	private BungeeDebugger debugger;
	private HandshakeHandler handshakeHandler;

	@Override
	public void onLoad() {
		instance = this;
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		config = FloodgateConfig.load(getLogger(), getDataFolder().toPath().resolve("config.yml"));
		UserConfig.load(getLogger(), getDataFolder().toPath().resolve("user.yml"));
		handshakeHandler = new HandshakeHandler(config);
	}

	@Override
	public void onEnable() {
		getProxy().getPluginManager().registerListener(this, this);
		if (config.isDebug()) {
			debugger = new BungeeDebugger();
		}
	}

	@Override
	public void onDisable() {
		if (config.isDebug()) {
			getLogger().warning(
					"Please note that it is not possible to reload this plugin when debug mode is enabled. At least for now");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPreLogin(PreLoginEvent event) {
		event.registerIntent(this);
		getProxy().getScheduler().runAsync(this, () -> {
			String host = ReflectionUtil.getCastedValue(event.getConnection(), handshake, Handshake.class).getHost();
			System.out.println(host);
			HandshakeResult result = handshakeHandler.handle(host, event.getConnection());
			System.out.println(result);
			System.out.println(result.getResultType().toString());
			switch (result.getResultType()) {
			case SUCCESS:
				break;
			case INVALID_USERNAME:
				event.setCancelReason(config.getMessages().getInvalidName());
				event.setCancelled(true);
				break;
			case INVALID_ADDRESS:
				event.setCancelReason(config.getMessages().getInvalidAddress());
				event.setCancelled(true);
				break;
			case NOT_SECONDAUTHER_DATA:
				break;
			}

			if (result.getResultType() != ResultType.SUCCESS) {
				// only continue when SUCCESS
				event.completeIntent(this);
				return;
			}

			FloodgatePlayer player = result.getFloodgatePlayer();

			event.getConnection().setOnlineMode(false);
			event.getConnection().setUniqueId(player.getJavaUniqueId());

			ReflectionUtil.setValue(event.getConnection(), "name", player.getJavaUsername());
			event.completeIntent(this);
		});
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
	}

	static {
		ReflectionUtil.setPrefix("net.md_5.bungee");
		Class<?> initial_handler = ReflectionUtil.getPrefixedClass("connection.InitialHandler");
		handshake = ReflectionUtil.getField(initial_handler, "handshake");
	}
}
