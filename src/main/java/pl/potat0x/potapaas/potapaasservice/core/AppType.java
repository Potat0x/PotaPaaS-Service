package pl.potat0x.potapaas.potapaasservice.core;

public enum AppType {
    NODEJS("Node.js (NPM)");

    public final String userFriendlyName;

    AppType(String userFriendlyName) {
        this.userFriendlyName = userFriendlyName;
    }
}
