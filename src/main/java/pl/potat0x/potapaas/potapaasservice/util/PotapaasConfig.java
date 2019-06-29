package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.Function0;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class PotapaasConfig {

    public static String get(String propertyName) {
        return properties.get().get(propertyName).toString();
    }

    private static Function0<Properties> properties = Function0.of(PotapaasConfig::loadProperties).memoized();

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