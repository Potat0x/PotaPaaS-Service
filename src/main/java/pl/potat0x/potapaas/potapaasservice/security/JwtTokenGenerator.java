package pl.potat0x.potapaas.potapaasservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

import java.util.Date;

final class JwtTokenGenerator {
    static String generateJwtToken(String username, byte[] jwtSecret) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpirationTimeInMillis()))
                .sign(Algorithm.HMAC512(jwtSecret));
    }

    private static long tokenExpirationTimeInMillis() {
        return PotapaasConfig.getInt("jwt_token_expiration_time_in_seconds") * 1000;
    }
}
