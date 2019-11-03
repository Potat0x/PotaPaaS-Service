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
    private boolean autodeployEnabled;
    private String commitHash;
    private String datastoreUuid;
    private LocalDateTime createdAt;

    public AppEntity(AppInstanceEntity appInstance, AppType type, String uuid, String name, String sourceRepoUrl, String sourceBranchName, boolean autodeployEnabled, String commitHash, String datastoreUuid) {
        this.appInstance = appInstance;
        this.type = type;
        this.uuid = uuid;
        this.name = name;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.autodeployEnabled = autodeployEnabled;
        this.commitHash = commitHash;
        this.datastoreUuid = datastoreUuid;
        this.createdAt = LocalDateTime.now();
    }

    protected AppEntity() {
    }

    public String getUuid() {
        return uuid;
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

    public boolean isAutodeployEnabled() {
        return autodeployEnabled;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getDatastoreUuid() {
        return datastoreUuid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setAppInstance(AppInstanceEntity appInstance) {
        this.appInstance = appInstance;
    }

    public void setType(AppType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSourceRepoUrl(String sourceRepoUrl) {
        this.sourceRepoUrl = sourceRepoUrl;
    }

    public void setSourceBranchName(String sourceBranchName) {
        this.sourceBranchName = sourceBranchName;
    }

    public void setAutodeployEnabled(boolean autodeployEnabled) {
        this.autodeployEnabled = autodeployEnabled;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public void setDatastoreUuid(String datastoreUuid) {
        this.datastoreUuid = datastoreUuid;
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
