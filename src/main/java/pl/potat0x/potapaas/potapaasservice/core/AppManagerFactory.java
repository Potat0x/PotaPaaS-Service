package pl.potat0x.potapaas.potapaasservice.core;

public final class AppManagerFactory {

    private final String dockerApiUrl;
    private final GitCloner gitCloner;
    private final boolean imageBuildingCache;

    public AppManagerFactory(GitCloner gitCloner, String dockerApiUrl) {
        this(gitCloner, dockerApiUrl, false);
    }

    public AppManagerFactory(GitCloner gitCloner, String dockerApiUrl, boolean imageBuildingCache) {
        this.gitCloner = gitCloner;
        this.dockerApiUrl = dockerApiUrl;
        this.imageBuildingCache = imageBuildingCache;
    }

    public AppManager createApp(AppType appType, String name, String gitRepoUrl, String repoBranchName) {
        return AppManager.createApp(
                gitCloner,
                new DockerContainerManager(dockerApiUrl),
                new DockerImageManager(dockerApiUrl, appType, imageBuildingCache),
                name, appType, gitRepoUrl, repoBranchName
        );
    }

    public AppManager forExistingApp(AppType appType, String appUuid, String name, String gitRepoUrl, String repoBranchName, String containerId, String imageId) {
        return AppManager.forExistingApp(
                gitCloner,
                new DockerContainerManager(dockerApiUrl),
                new DockerImageManager(dockerApiUrl, appType, imageBuildingCache),
                appUuid, name, appType, gitRepoUrl, repoBranchName, containerId, imageId
        );
    }
}
