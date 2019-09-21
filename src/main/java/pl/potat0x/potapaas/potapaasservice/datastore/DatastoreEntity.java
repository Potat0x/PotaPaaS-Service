package pl.potat0x.potapaas.potapaasservice.datastore;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "datastore")
class DatastoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DatastoreType type;

    private String uuid;
    private String name;
    private String username;
    private String password;
    private String containerId;

    public DatastoreEntity(DatastoreType type, String name, String username, String password, String containerId) {
        this.uuid = UUID.randomUUID().toString();
        this.type = type;
        this.name = name;
        this.username = username;
        this.password = password;
        this.containerId = containerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatastoreEntity that = (DatastoreEntity) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}