package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
final class AppResponseDto {
    private final String appUuid;
    private final String name;
    private final String type;
    private final String sourceRepoUrl;
    private final String sourceBranchName;
    private final LocalDateTime createdAt;
    private final String status;
    private final int exposedPort;
    private final String datastoreUuid;

    @JsonCreator
    public AppResponseDto(@JsonProperty("appUuid") String appUuid,
                          @JsonProperty("name") String name,
                          @JsonProperty("type") String type,
                          @JsonProperty("sourceRepoUrl") String sourceRepoUrl,
                          @JsonProperty("sourceBranchName") String sourceBranchName,
                          @JsonProperty("createdAt") LocalDateTime createdAt,
                          @JsonProperty("status") String status,
                          @JsonProperty("exposedPort") int exposedPort,
                          @JsonProperty("datastoreUuid") String datastoreUuid) {
        this.appUuid = appUuid;
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.createdAt = createdAt;
        this.status = status;
        this.exposedPort = exposedPort;
        this.datastoreUuid = datastoreUuid;
    }

    public String getAppUuid() {
        return appUuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
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

    public String getStatus() {
        return status;
    }

    public int getExposedPort() {
        return exposedPort;
    }

    public String getDatastoreUuid() {
        return datastoreUuid;
    }
}
