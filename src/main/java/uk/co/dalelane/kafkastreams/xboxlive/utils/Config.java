package uk.co.dalelane.kafkastreams.xboxlive.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    private static Logger log = LoggerFactory.getLogger(Config.class);

    public static final String XBOX_API_KEY = "xbl.api.key";

    public static Properties getStreamsConfiguration() {
        log.info("Reading config from app.properties");
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream("app.properties"));
        }
        catch (FileNotFoundException e) {
            log.error("app.properties config file not found", e);
            System.exit(-1);
        }
        catch (IOException e) {
            log.error("app.properties file could not be loaded", e);
            System.exit(-2);
        }

        appProps.forEach((key, val) -> log.debug("{} = {}", key, val));

        if (!appProps.containsKey(XBOX_API_KEY)) {
            log.error("app.properties doesn't contain required property " + XBOX_API_KEY);
            System.exit(-3);
        }

        return appProps;
    }
}
