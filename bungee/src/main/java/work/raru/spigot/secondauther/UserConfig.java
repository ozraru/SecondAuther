package work.raru.spigot.secondauther;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class UserConfig {
	@Getter static Configuration config = null;
	public static Configuration load(Logger logger, Path configPath) {
		try {
			try {
				if (!configPath.toFile().exists()) {
					Files.copy(FloodgateConfig.class.getClassLoader().getResourceAsStream("user.yml"), configPath);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error while creating config", e);
			}

			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configPath.toFile());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while loading config", e);
		}

		if (config == null) {
			throw new RuntimeException("Failed to load config file! Try to delete the data folder of Floodgate");
		}
		return config;
	}
}
