package pl.potat0x.potapaas.potapaasservice.app;

public final class AppRequestDtoBuilder {

    private String name;
    private String type;
    private String sourceRepoUrl;
    private String sourceBranchName;
    private boolean autodeployEnabled;
    private String commitHash;
    private String datastoreUuid;

    public AppRequestDto build() {
        return new AppRequestDto(name, type, sourceRepoUrl, sourceBranchName, autodeployEnabled, commitHash, datastoreUuid);
    }

    public AppRequestDtoBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AppRequestDtoBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public AppRequestDtoBuilder withSourceRepoUrl(String sourceRepoUrl) {
        this.sourceRepoUrl = sourceRepoUrl;
        return this;
    }

    public AppRequestDtoBuilder withSourceBranchName(String sourceBranchName) {
        this.sourceBranchName = sourceBranchName;
        return this;
    }

    public AppRequestDtoBuilder withAutodeployEnabled(boolean autodeployEnabled) {
        this.autodeployEnabled = autodeployEnabled;
        return this;
    }

    public AppRequestDtoBuilder withCommitHash(String commitHash) {
        this.commitHash = commitHash;
        return this;
    }

    public AppRequestDtoBuilder withDatastoreUuid(String datastoreUuid) {
        this.datastoreUuid = datastoreUuid;
        return this;
    }
}
