package pl.potat0x.potapaas.potapaasservice.system;

import io.vavr.Function0;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PotapaasConfig {

    public static String get(String propertyName) {
        return properties.get().get(propertyName).toString();
    }

    public static Integer getInt(String propertyName) {
        return Integer.parseInt(properties.get().get(propertyName).toString());
    }

    private static final Function0<Properties> properties = Function0.of(PotapaasConfig::loadProperties).memoized();

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream is = PotapaasConfig.class.getResourceAsStream("/application.properties")) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
