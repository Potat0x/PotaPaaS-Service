package pl.potat0x.potapaas.potapaasservice.app;

public final class AppRequestDtoBuilder {

    private String name;
    private String type;
    private String sourceRepoUrl;
    private String sourceBranchName;
    private String datastoreUuid;

    public AppRequestDto build() {
        return new AppRequestDto(name, type, sourceRepoUrl, sourceBranchName, datastoreUuid);
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

    public AppRequestDtoBuilder withDatastoreUuid(String datastoreUuid) {
        this.datastoreUuid = datastoreUuid;
        return this;
    }
}
