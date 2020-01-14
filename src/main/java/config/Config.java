package config;

import template.Template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static final String CONFIG_PATH = "templateCode/config.properties";

    private static final String SAVE_PATH = "file.savePath";

    private static final Properties PROP;

    public static String driver;

    public static String url;

    public static String username;

    public static String password;

    static {
        PROP = new Properties();
        InputStream in = Config.class.getClassLoader().getResourceAsStream(CONFIG_PATH);
        try {
            PROP.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        driver = PROP.getProperty("jdbc.driver");
        url = PROP.getProperty("jdbc.url");
        username = PROP.getProperty("jdbc.username");
        password = PROP.getProperty("jdbc.password");
    }

    public static void initConfig() throws IOException {
        initCon();
    }

    private static void initCon() throws IOException {
        Template.createTemplate();
    }

    public static String getSavePath(){
        return PROP.getProperty(SAVE_PATH);
    }

}
