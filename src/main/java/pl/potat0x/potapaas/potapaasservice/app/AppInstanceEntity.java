package pl.potat0x.potapaas.potapaasservice.app;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "app_instance")
class AppInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "appInstance")
    private AppEntity appEntity;

    private String containerId;
    private String imageId;

    private String uuid;

    public AppInstanceEntity(String containerId, String imageId) {
        this.uuid = UUID.randomUUID().toString();
        this.imageId = imageId;
        this.containerId = containerId;
    }

    protected AppInstanceEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getImageId() {
        return imageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppInstanceEntity that = (AppInstanceEntity) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
