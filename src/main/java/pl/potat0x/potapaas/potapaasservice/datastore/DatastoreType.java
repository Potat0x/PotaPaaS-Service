package pl.potat0x.potapaas.potapaasservice.datastore;

public enum DatastoreType {
    POSTGRESQL("PostgreSQL", "postgres", "5432/tcp", "postgres", "postgres"),
    MYSQL("MySQL", "mysql", "3306/tcp", "root", "mysql"),
    MARIADB("MariaDB", "mariadb", "3306/tcp", "root", "mysql");

    public final String userFriendlyName;
    public final String dockerRepository;
    public final String defaultPortAndProtocol;
    public final int defaultPort;
    public final String defaultUsername;
    public final String databaseName;

    DatastoreType(String userFriendlyName, String dockerRepository, String defaultPortAndProtocol, String defaultUsername, String databaseName) {
        this.userFriendlyName = userFriendlyName;
        this.dockerRepository = dockerRepository;
        this.defaultPortAndProtocol = defaultPortAndProtocol;
        this.defaultPort = Integer.parseInt(defaultPortAndProtocol.contains("/") ? defaultPortAndProtocol.substring(0, defaultPortAndProtocol.indexOf("/")) : defaultPortAndProtocol);
        this.defaultUsername = defaultUsername;
        this.databaseName = databaseName;
    }
}
