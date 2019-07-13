package pl.potat0x.potapaas.potapaasservice.core;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class AppDeployment {

    private final String githubRepoUrl;
    private final String branchName;
    private final AppType appType;
    private final String potapaasAppId;
    private String containerId;
    private String appSourceDir;

    private final DockerContainerManager containerManager;

    public AppDeployment(AppType appType, String githubRepoUrl, String branchName) {
        containerManager = new DockerContainerManager(PotapaasConfig.get("docker_api_uri"));
        potapaasAppId = UUID.randomUUID().toString();
        this.githubRepoUrl = githubRepoUrl;
        this.branchName = branchName;
        this.appType = appType;
    }

    public Either<ErrorMessage, String> deploy() {
        return cloneRepo()
                .map(clonedRepoDir -> this.appSourceDir = clonedRepoDir)
                .flatMap(clonedRepoDir -> buildTestImage())
                .flatMap(this::runAppTests)
                .flatMap(testResults -> buildReleaseImage())
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

    private Either<ErrorMessage, String> runApp(String imageId) {
        return runContainer(imageId, DockerImageManager.BuildType.RELEASE);
    }

    private Either<ErrorMessage, String> runAppTests(String imageId) {
        return runContainer(imageId, DockerImageManager.BuildType.TEST)
                .flatMap(containerManager::waitForExit)
                .flatMap(exitCode -> exitCode == 0 ? Either.right("Tests passed!") : Either.left(CoreErrorMessage.APP_TESTS_FAIL));
    }

    private Either<ErrorMessage, String> buildReleaseImage() {
        return buildImage(appSourceDir, DockerImageManager.BuildType.RELEASE);
    }

    private Either<ErrorMessage, String> buildTestImage() {
        return buildImage(appSourceDir, DockerImageManager.BuildType.TEST);
    }

    private Either<ErrorMessage, String> runContainer(String imageId, DockerImageManager.BuildType buildType) {
        HostConfig hostConfig = HostConfig.builder()
                .publishAllPorts(buildType == DockerImageManager.BuildType.RELEASE)
                .build();

        Map<String, String> labels = Map.of(PotapaasConfig.get("containers_label_prefix") + buildType,
                potapaasAppId.substring(0, PotapaasConfig.getInt("deployment_uuid_label_length")));

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
                    .flatMap(cloner -> cloner.cloneBranch(githubRepoUrl, branchName));
        } catch (Exception e) {
            return ExceptionMapper.map(e).of();
        }
    }
}
