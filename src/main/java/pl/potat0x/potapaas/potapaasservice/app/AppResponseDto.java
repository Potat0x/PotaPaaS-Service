package pl.potat0x.potapaas.potapaasservice.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode
final class AppResponseDto {
    private final String name;
    private final String type;
    private final String sourceRepoUrl;
    private final String sourceBranchName;
    private final LocalDateTime createdAt;
    private final String status;
    private final String uptime;
    private final int exposedPort;

    @JsonCreator
    public AppResponseDto(@JsonProperty("name") String name,
                          @JsonProperty("type") String type,
                          @JsonProperty("sourceRepoUrl") String sourceRepoUrl,
                          @JsonProperty("sourceBranchName") String sourceBranchName,
                          @JsonProperty("createdAt") LocalDateTime createdAt,
                          @JsonProperty("status") String status,
                          @JsonProperty("uptime") String uptime,
                          @JsonProperty("exposedPort") int exposedPort) {
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.createdAt = createdAt;
        this.status = status;
        this.uptime = uptime;
        this.exposedPort = exposedPort;
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

    public String getUptime() {
        return uptime;
    }

    public int getExposedPort() {
        return exposedPort;
    }
}
