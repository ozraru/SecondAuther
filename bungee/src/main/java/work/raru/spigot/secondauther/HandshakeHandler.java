package work.raru.spigot.secondauther;

import lombok.*;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.config.Configuration;

import java.util.List;
import java.util.UUID;

public class HandshakeHandler {
	private String usernamePrefix;
	private boolean replaceSpaces;
	private String hostnameRegex;

	public HandshakeHandler(FloodgateConfig config) {
		this.usernamePrefix = config.getUsernamePrefix();
		this.replaceSpaces = config.isReplaceSpaces();
		this.hostnameRegex = config.getHostnameRegex();
	}

	public HandshakeResult handle(@NonNull String handshakeData, @NonNull PendingConnection conn) {
		String[] data = handshakeData.split("[.]", 2);
		if (data.length != 2 || !data[1].matches(hostnameRegex)) {
			return ResultType.NOT_SECONDAUTHER_DATA.getCachedResult();
		}

		Configuration user = UserConfig.getConfig().getSection("users." + data[0]);
		if (user == null) {
			return ResultType.INVALID_USERNAME.getCachedResult();
		}
		List<String> ipaddrs = user.getStringList("ip");
		String socketaddr = conn.getSocketAddress().toString().split(":")[0].replace("/", "");
		for (String ip : ipaddrs) {
			if (socketaddr.equals(ip)) {
				UUID uuid = UUID.fromString(user.getString("UUID"));
				FloodgatePlayer player = new FloodgatePlayer(uuid, data[0], usernamePrefix, replaceSpaces);
				return new HandshakeResult(ResultType.SUCCESS, player);
			}
		}
		return ResultType.INVALID_ADDRESS.getCachedResult();
	}

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	@Getter
	@ToString
	public static class HandshakeResult {
		private ResultType resultType;
		private FloodgatePlayer floodgatePlayer;
	}

	public enum ResultType {
		NOT_SECONDAUTHER_DATA, INVALID_USERNAME, INVALID_ADDRESS, SUCCESS;

		@Getter
		private HandshakeResult cachedResult;

		ResultType() {
			cachedResult = new HandshakeResult(this, null);
		}
	}
}
