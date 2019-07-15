package pl.potat0x.potapaas.potapaasservice.core;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AppManager {

    private final String appName;
    private final String gitRepoUrl;
    private final String branchName;
    private final AppType appType;
    private final String potapaasAppId;
    private String containerId;
    private String imageId;
    private String clonedRepoDir;

    private final DockerContainerManager containerManager;

    public static AppManager createApp(String name, AppType type, String gitRepoUrl, String repoBranchName) {
        return new AppManager(UUID.randomUUID().toString(), name, type, gitRepoUrl, repoBranchName);
    }

    public static AppManager forExistingApp(String appUuid, String name, AppType type, String gitRepoUrl, String repoBranchName, String containerId, String imageId) {
        return new AppManager(appUuid, name, type, gitRepoUrl, repoBranchName, containerId, imageId);
    }

    public Either<ErrorMessage, String> deploy() {
        return cloneRepo()
                .map(clonedRepoDir -> this.clonedRepoDir = clonedRepoDir)
                .flatMap(clonedRepoDir -> buildTestImage())
                .flatMap(this::runAppTests)
                .flatMap(testResults -> buildReleaseImage())
                .map(imageId -> this.imageId = imageId)
                .flatMap(imageId -> runApp(imageId)
                        .map(containerId -> this.containerId = containerId)
                        .map(containerId -> potapaasAppId)
                );
    }

    public Either<ErrorMessage, String> killApp() {
        return containerManager.killContainer(containerId);
    }

    public Either<ErrorMessage, String> getPort() {
        return containerManager.getHostPort(containerId);
    }

    public Either<ErrorMessage, String> getLogs() {
        return containerManager.getLogs(containerId);
    }

    public Either<ErrorMessage, String> getStatus() {
        return containerManager.getStatus(containerId);
    }

    public Either<ErrorMessage, LocalDateTime> getCreationDate() {
        return containerManager.getCreationDate(containerId);
    }

    public String getAppName() {
        return appName;
    }

    public String getGitRepoUrl() {
        return gitRepoUrl;
    }

    public String getBranchName() {
        return branchName;
    }

    public AppType getAppType() {
        return appType;
    }

    public String getPotapaasAppId() {
        return potapaasAppId;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getImageId() {
        return imageId;
    }

    private AppManager(String potapaasAppId, String name, AppType type, String gitRepoUrl, String branchName) {
        containerManager = new DockerContainerManager(PotapaasConfig.get("docker_api_uri"));
        this.appName = name;
        this.gitRepoUrl = gitRepoUrl;
        this.branchName = branchName;
        this.appType = type;
        this.potapaasAppId = potapaasAppId;
    }

    private AppManager(String potapaasAppId, String name, AppType type, String gitRepoUrl, String branchName, String containerId, String imageId) {
        this(potapaasAppId, name, type, gitRepoUrl, branchName);
        this.containerId = containerId;
        this.imageId = imageId;
    }

    private Either<ErrorMessage, String> runApp(String imageId) {
        return runContainer(imageId, DockerImageManager.BuildType.RELEASE);
    }

    private Either<ErrorMessage, String> runAppTests(String imageId) {
        return runContainer(imageId, DockerImageManager.BuildType.TEST)
                .flatMap(containerManager::waitForExit)
                .flatMap(exitCode -> exitCode == 0 ? Either.right("Tests passed!") : Either.left(CoreErrorMessage.APP_TESTS_FAIL));
    }

    private Either<ErrorMessage, String> buildReleaseImage() {
        return buildImage(clonedRepoDir, DockerImageManager.BuildType.RELEASE);
    }

    private Either<ErrorMessage, String> buildTestImage() {
        return buildImage(clonedRepoDir, DockerImageManager.BuildType.TEST);
    }

    private Either<ErrorMessage, String> runContainer(String imageId, DockerImageManager.BuildType buildType) {
        HostConfig hostConfig = HostConfig.builder()
                .publishAllPorts(buildType == DockerImageManager.BuildType.RELEASE)
                .build();

        Map<String, String> labels = new HashMap<>();
        labels.put(PotapaasConfig.get("container_label_app_name"), appName);
        labels.put(PotapaasConfig.get("container_label_app_type"), appType.toString());
        labels.put(PotapaasConfig.get("container_label_build_type"), buildType.toString());

        ContainerConfig.Builder config = ContainerConfig.builder()
                .image(imageId)
                .exposedPorts(PotapaasConfig.get("default_webapp_port"))
                .hostConfig(hostConfig)
                .labels(labels);

        return containerManager.runContainer(config);
    }

    private Either<ErrorMessage, String> buildImage(String appSourceDir, DockerImageManager.BuildType buildType) {
        DockerImageManager imageManager = new DockerImageManager(PotapaasConfig.get("docker_api_uri"), appSourceDir, appType);
        return imageManager.buildImage(buildType);
    }

    private Either<ErrorMessage, String> cloneRepo() {
        try {
            Path tmpDir = Files.createTempDirectory(PotapaasConfig.get("tmp_git_dir_prefix"));
            return GitCloner.create(tmpDir.toAbsolutePath())
                    .flatMap(cloner -> cloner.cloneBranch(gitRepoUrl, branchName));
        } catch (Exception e) {
            return ExceptionMapper.map(e).of();
        }
    }
}
