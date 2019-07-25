package pl.potat0x.potapaas.potapaasservice.app;

import java.time.LocalDateTime;

final class AppResponseDtoBuilder {

    private String appUuid;
    private String name;
    private String type;
    private String sourceRepoUrl;
    private String sourceBranchName;
    private LocalDateTime createdAt;
    private String status;
    private int exposedPort;

    AppResponseDto build() {
        return new AppResponseDto(appUuid, name, type, sourceRepoUrl, sourceBranchName, createdAt, status, exposedPort);
    }

    public AppResponseDtoBuilder withAppUuid(String appUuid) {
        this.appUuid = appUuid;
        return this;
    }

    public AppResponseDtoBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AppResponseDtoBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public AppResponseDtoBuilder withSourceRepoUrl(String sourceRepoUrl) {
        this.sourceRepoUrl = sourceRepoUrl;
        return this;
    }

    public AppResponseDtoBuilder withSourceBranchName(String sourceBranchName) {
        this.sourceBranchName = sourceBranchName;
        return this;
    }

    public AppResponseDtoBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public AppResponseDtoBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public AppResponseDtoBuilder withExposedPort(int exposedPort) {
        this.exposedPort = exposedPort;
        return this;
    }
}
