package pl.potat0x.potapaas.potapaasservice.security;

public final class Principal {
    public final String username;
    public final Long userId;

    public Principal(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }
}
