package pl.potat0x.potapaas.potapaasservice.app;

import pl.potat0x.potapaas.potapaasservice.core.AppType;

import java.time.LocalDateTime;

final class AppResponseDtoBuilder {

    private String appUuid;
    private String name;
    private AppType type;
    private String sourceRepoUrl;
    private String sourceBranchName;
    private boolean autodeployEnabled;
    private String webhookSecret;
    private LocalDateTime createdAt;
    private String status;
    private int exposedPort;
    private String commitHash;
    private String datastoreUuid;

    AppResponseDto build() {
        return new AppResponseDto(appUuid, name, type, sourceRepoUrl, sourceBranchName, autodeployEnabled, webhookSecret, createdAt, status, exposedPort, commitHash, datastoreUuid);
    }

    public AppResponseDtoBuilder withAppUuid(String appUuid) {
        this.appUuid = appUuid;
        return this;
    }

    public AppResponseDtoBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AppResponseDtoBuilder withType(AppType type) {
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

    public AppResponseDtoBuilder withAutodeployEnabled(boolean autodeployEnabled) {
        this.autodeployEnabled = autodeployEnabled;
        return this;
    }

    public AppResponseDtoBuilder withWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
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

    public AppResponseDtoBuilder withCommitHash(String commitHash) {
        this.commitHash = commitHash;
        return this;
    }

    public AppResponseDtoBuilder withDatastoreUuid(String datastoreUuid) {
        this.datastoreUuid = datastoreUuid;
        return this;
    }
}
