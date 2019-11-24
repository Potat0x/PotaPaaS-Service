package pl.potat0x.potapaas.potapaasservice.security;

final class Principal {
    public final String username;
    public final Long userId;

    Principal(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }
}
