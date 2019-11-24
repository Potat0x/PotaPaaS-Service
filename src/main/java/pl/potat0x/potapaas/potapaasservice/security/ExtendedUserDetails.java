package pl.potat0x.potapaas.potapaasservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public final class ExtendedUserDetails extends User {
    private final long userId;

    public ExtendedUserDetails(String username, String password, long userId, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}
