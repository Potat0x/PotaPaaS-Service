package pl.potat0x.potapaas.potapaasservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

final class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final byte[] jwtSecret;

    JwtAuthorizationFilter(AuthenticationManager authenticationManager, byte[] jwtSecret) {
        super(authenticationManager);
        this.jwtSecret = jwtSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!isBearerTokenPresentInAuthHeader(request)) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken = getAuthenticationToken(request);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthenticationToken(HttpServletRequest request) {
        String bearerToken = getBearerTokenValueFromAuthHeader(request);
        if (bearerToken != null) {
            DecodedJWT decodedToken = JWT.require(Algorithm.HMAC512(jwtSecret))
                    .build()
                    .verify(bearerToken);

            String username = decodedToken.getSubject();
            if (username != null) {
                return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            }
        }
        return null;
    }

    private String getBearerTokenValueFromAuthHeader(HttpServletRequest request) {
        String headerValue = request.getHeader("Authorization");
        if (headerValue == null || !headerValue.startsWith("Bearer ")) {
            return null;
        }
        return headerValue.replace("Bearer ", "");
    }

    private boolean isBearerTokenPresentInAuthHeader(HttpServletRequest request) {
        return getBearerTokenValueFromAuthHeader(request) != null;
    }
}
