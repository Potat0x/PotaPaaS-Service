package pl.potat0x.potapaas.potapaasservice.datastore;

public enum DatastoreType {
    POSTGRES("PostgreSQL", "postgres");

    public final String userFriendlyName;
    public final String dockerRepository;

    DatastoreType(String userFriendlyName, String dockerRepository) {
        this.userFriendlyName = userFriendlyName;
        this.dockerRepository = dockerRepository;
    }
}
