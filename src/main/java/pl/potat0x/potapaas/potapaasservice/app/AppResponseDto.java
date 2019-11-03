package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import pl.potat0x.potapaas.potapaasservice.core.AppType;

import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
public final class AppResponseDto {
    private final String appUuid;
    private final String name;
    private final AppType type;
    private final String sourceRepoUrl;
    private final String sourceBranchName;
    private final boolean autodeployEnabled;
    private final LocalDateTime createdAt;
    private final String status;
    private final int exposedPort;
    private final String commitHash;
    private final String datastoreUuid;

    @JsonCreator
    public AppResponseDto(@JsonProperty("appUuid") String appUuid,
                          @JsonProperty("name") String name,
                          @JsonProperty("type") AppType type,
                          @JsonProperty("sourceRepoUrl") String sourceRepoUrl,
                          @JsonProperty("sourceBranchName") String sourceBranchName,
                          @JsonProperty("autodeployEnabled") boolean autodeployEnabled,
                          @JsonProperty("createdAt") LocalDateTime createdAt,
                          @JsonProperty("status") String status,
                          @JsonProperty("exposedPort") int exposedPort,
                          @JsonProperty("commitHash") String commitHash,
                          @JsonProperty("datastoreUuid") String datastoreUuid) {
        this.appUuid = appUuid;
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.autodeployEnabled = autodeployEnabled;
        this.createdAt = createdAt;
        this.status = status;
        this.exposedPort = exposedPort;
        this.commitHash = commitHash;
        this.datastoreUuid = datastoreUuid;
    }

    public String getAppUuid() {
        return appUuid;
    }

    public String getName() {
        return name;
    }

    public AppType getType() {
        return type;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public int getExposedPort() {
        return exposedPort;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getDatastoreUuid() {
        return datastoreUuid;
    }
}
