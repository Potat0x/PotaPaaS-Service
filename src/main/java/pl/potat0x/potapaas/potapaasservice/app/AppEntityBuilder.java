package pl.potat0x.potapaas.potapaasservice.app;

import pl.potat0x.potapaas.potapaasservice.core.AppType;

final class AppEntityBuilder {

    private AppInstanceEntity appInstance;
    private AppType type;
    private String uuid;
    private String name;
    private String sourceRepoUrl;
    private String sourceBranchName;

    AppEntity build() {
        return new AppEntity(appInstance, type, uuid, name, sourceRepoUrl, sourceBranchName);
    }

    AppEntityBuilder withAppInstance(AppInstanceEntity appInstance) {
        this.appInstance = appInstance;
        return this;
    }

    AppEntityBuilder withType(AppType type) {
        this.type = type;
        return this;
    }

    AppEntityBuilder withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    AppEntityBuilder withName(String name) {
        this.name = name;
        return this;
    }

    AppEntityBuilder withSourceRepoUrl(String sourceRepoUrl) {
        this.sourceRepoUrl = sourceRepoUrl;
        return this;
    }

    AppEntityBuilder withSourceBranchName(String sourceBranchName) {
        this.sourceBranchName = sourceBranchName;
        return this;
    }
}
