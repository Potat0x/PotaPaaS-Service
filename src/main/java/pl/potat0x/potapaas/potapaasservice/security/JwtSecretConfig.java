package pl.potat0x.potapaas.potapaasservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

import java.security.SecureRandom;

@Configuration
class JwtSecretConfig {

    private final byte[] jwtSecret = generateJwtSecret();

    @Bean
    byte[] getJwtSecret() {
        return jwtSecret;
    }

    private byte[] generateJwtSecret() {
        byte[] randomBytes = new byte[PotapaasConfig.getInt("jwt_secret_size_in_bytes")];
        new SecureRandom().nextBytes(randomBytes);
        return randomBytes;
    }
}
