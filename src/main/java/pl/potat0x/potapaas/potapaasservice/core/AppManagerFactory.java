package pl.potat0x.potapaas.potapaasservice.core;

import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

final public class AppManagerFactory {

    public static class AppManagerCreator {
        private final AppType appType;
        private final DockerContainerManager containerManager;
        private final DockerImageManager imageManager;
        private final GitCloner gitCloner;

        AppManagerCreator(GitCloner gitCloner, String dockerApiUrl, AppType appType) {
            this.appType = appType;
            this.gitCloner = gitCloner;
            containerManager = new DockerContainerManager(dockerApiUrl);
            imageManager = new DockerImageManager(PotapaasConfig.get("docker_api_uri"), appType);
        }

        public AppManager createApp(String name, String gitRepoUrl, String repoBranchName) {
            return AppManager.createApp(gitCloner, containerManager, imageManager, name, appType, gitRepoUrl, repoBranchName);
        }

        public AppManager forExistingApp(String appUuid, String name, String gitRepoUrl, String repoBranchName, String containerId, String imageId) {
            return AppManager.forExistingApp(gitCloner, containerManager, imageManager, appUuid, name, appType, gitRepoUrl, repoBranchName, containerId, imageId);
        }
    }

    public static AppManagerCreator defaultAppManager(AppType appType) {
        return new AppManagerCreator(new JGitCloner(), PotapaasConfig.get("docker_api_uri"), appType);
    }

    public static AppManagerCreator appManager(AppType appType, GitCloner gitCloner, String dockerApiUrl) {
        return new AppManagerCreator(gitCloner, dockerApiUrl, appType);
    }
}
