package pl.potat0x.potapaas.potapaasservice.app;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Objects;
import java.util.UUID;

@Entity
public class AppInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "appInstance")
    private App app;

    private String containerId;
    private String imageId;

    private String uuid;

    public AppInstance(String containerId, String imageId) {
        this.uuid = UUID.randomUUID().toString();
        this.imageId = imageId;
        this.containerId = containerId;
    }

    protected AppInstance() {
    }

    public App getApp() {
        return app;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppInstance that = (AppInstance) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
