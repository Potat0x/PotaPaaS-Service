package pl.potat0x.potapaas.potapaasservice.core;

import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;

import java.util.UUID;

public final class AppManagerFactory {

    private final String dockerApiUrl;
    private final GitCloner gitCloner;
    private final boolean imageBuildingCache;

    public AppManagerFactory(GitCloner gitCloner, String dockerApiUrl, boolean imageBuildingCache) {
        this.gitCloner = gitCloner;
        this.dockerApiUrl = dockerApiUrl;
        this.imageBuildingCache = imageBuildingCache;
    }

    public AppManagerFactory(GitCloner gitCloner, String dockerApiUrl) {
        this(gitCloner, dockerApiUrl, false);
    }

    public AppManager forNewApp(AppRequestDto appRequestDto) {
        return new AppManager(new DockerContainerManager(dockerApiUrl), new DockerImageManager(dockerApiUrl, AppType.valueOf(appRequestDto.getType()), imageBuildingCache), gitCloner, appRequestDto, UUID.randomUUID().toString());
    }

    public AppManager forExistingApp(AppRequestDto requestDto, AppType appType, String appUuid, String containerId) {
        return new AppManager(new DockerContainerManager(dockerApiUrl), new DockerImageManager(dockerApiUrl, appType, imageBuildingCache), gitCloner, requestDto, appUuid, containerId);
    }
}
