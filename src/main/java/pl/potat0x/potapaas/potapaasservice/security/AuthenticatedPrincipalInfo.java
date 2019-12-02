package pl.potat0x.potapaas.potapaasservice.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticatedPrincipalInfo {
    public static Long getUserId() {
        Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.userId;
    }
}
