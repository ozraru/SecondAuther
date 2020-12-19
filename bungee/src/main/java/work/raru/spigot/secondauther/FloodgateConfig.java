package work.raru.spigot.secondauther;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class FloodgateConfig {
	@JsonProperty(value = "username-prefix")
	private String usernamePrefix;
	@JsonProperty(value = "replace-spaces")
	private boolean replaceSpaces;
	@JsonProperty(value = "hostname-regex")
	private String hostnameRegex;
	@JsonProperty(value = "disconnect")
	private DisconnectMessages messages;

	@JsonProperty
	private boolean debug;

	@Getter
	public static class DisconnectMessages {
		@JsonProperty("invalid-name")
		private String invalidName;
		@JsonProperty("invalid-address")
		private String invalidAddress;
	}

	public static FloodgateConfig load(Logger logger, Path configPath) {
		return load(logger, configPath, FloodgateConfig.class);
	}

	public static <T extends FloodgateConfig> T load(Logger logger, Path configPath, Class<T> configClass) {
		T config = null;
		try {
			try {
				if (!configPath.toFile().exists()) {
					Files.copy(FloodgateConfig.class.getClassLoader().getResourceAsStream("config.yml"), configPath);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error while creating config", e);
			}

			config = new ObjectMapper(new YAMLFactory()).readValue(Files.readAllBytes(configPath), configClass);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while loading config", e);
		}

		if (config == null) {
			throw new RuntimeException("Failed to load config file! Try to delete the data folder of Floodgate");
		}

		return config;
	}
}
