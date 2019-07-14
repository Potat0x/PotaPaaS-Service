package pl.potat0x.potapaas.potapaasservice.app;

import pl.potat0x.potapaas.potapaasservice.core.AppType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "app")
class AppEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "app_instance_id", referencedColumnName = "id")
    private AppInstanceEntity appInstance;

    @Enumerated(value = EnumType.STRING)
    private AppType type;

    private String uuid;

    private String name;
    private String sourceRepoUrl;
    private String sourceBranchName;
    private LocalDateTime createdAt;

    public AppEntity(String uuid, AppInstanceEntity appInstance, String name, AppType type, String sourceRepoUrl, String sourceBranchName) {
        this.appInstance = appInstance;
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.uuid = uuid;
        this.createdAt = LocalDateTime.now();
    }

    protected AppEntity() {
    }

    public String getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }

    public AppInstanceEntity getAppInstance() {
        return appInstance;
    }

    public AppType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSourceRepoUrl() {
        return sourceRepoUrl;
    }

    public String getSourceBranchName() {
        return sourceBranchName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppEntity appEntity = (AppEntity) o;
        return uuid.equals(appEntity.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
