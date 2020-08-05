package app.unattach.model;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileConfig implements Config {
    private static final Logger LOGGER = Logger.getLogger(FileConfig.class.getName());
    private static final String FILENAME_SCHEMA_PROPERTY = "filename_schema";
    private static final String SEARCH_QUERY_PROPERTY = "search_query";
    private static final String TARGET_DIRECTORY_PROPERTY = "target_directory";

    private final Properties config;

    public FileConfig() {
        config = new Properties();
        loadConfigFromFile();
    }

    @Override
    public String getFilenameSchema() {
        return config.getProperty(FILENAME_SCHEMA_PROPERTY, FilenameFactory.DEFAULT_SCHEMA);
    }

    @Override
    public String getSearchQuery() {
        return config.getProperty(SEARCH_QUERY_PROPERTY, "has:attachment size:1m");
    }

    @Override
    public String getTargetDirectory() {
        return config.getProperty(TARGET_DIRECTORY_PROPERTY, getDefaultTargetDirectory());
    }

    @Override
    public void saveFilenameSchema(String schema) {
        config.setProperty(FILENAME_SCHEMA_PROPERTY, schema);
        saveConfigToFile();
    }

    @Override
    public void saveSearchQuery(String query) {
        config.setProperty(SEARCH_QUERY_PROPERTY, query);
        saveConfigToFile();
    }

    @Override
    public void saveTargetDirectory(String path) {
        config.setProperty(TARGET_DIRECTORY_PROPERTY, path);
        saveConfigToFile();
    }

    private void loadConfigFromFile() {
        File configFile = getConfigPath().toFile();
        if (configFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(configFile);
                config.load(in);
                in.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to load the config file.", e);
            }
        }
    }

    private void saveConfigToFile() {
        try {
            File configFile = getConfigPath().toFile();
            FileOutputStream out = new FileOutputStream(configFile);
            config.store(out, null);
            out.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save the config file.", e);
        }
    }

    private static Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "." + Constants.PRODUCT_NAME.toLowerCase() + ".properties");
    }

    private static String getDefaultTargetDirectory() {
        String userHome = System.getProperty("user.home");
        Path defaultPath = Paths.get(userHome, "Downloads", Constants.PRODUCT_NAME);
        return defaultPath.toString();
    }
}
