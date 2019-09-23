package pl.potat0x.potapaas.potapaasservice.core;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.app.AppRequestDto;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class AppManager {

    private final DockerContainerManager containerManager;
    private final DockerImageManager imageManager;
    private final GitCloner gitCloner;
    private AppRequestDto requestDto;

    private String appUuid;
    private String imageId;
    private String containerId;
    private String clonedRepoDir;

    AppManager(DockerContainerManager containerManager, DockerImageManager imageManager, GitCloner gitCloner, AppRequestDto requestDto, String appUuid) {
        this.containerManager = containerManager;
        this.imageManager = imageManager;
        this.gitCloner = gitCloner;

        this.requestDto = requestDto;
        this.appUuid = appUuid;
    }

    AppManager(DockerContainerManager containerManager, DockerImageManager imageManager, GitCloner gitCloner, AppRequestDto requestDto, String appUuid, String containerId) {
        this(containerManager, imageManager, gitCloner, requestDto, appUuid);
        this.containerId = containerId;
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
                        .map(containerId -> appUuid)
                );
    }

    public Either<ErrorMessage, String> redeploy() {
        String oldContainerId = containerId;
        return deploy()
                .peek(appUuid -> stopContainer(oldContainerId));
    }

    public String getAppUuid() {
        return appUuid;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getImageId() {
        return imageId;
    }

    public Either<ErrorMessage, String> getPort() {
        return containerManager.getHostPort(containerId, PotapaasConfig.get("default_webapp_port"));
    }

    public Either<ErrorMessage, String> getLogs() {
        return containerManager.getLogs(containerId);
    }

    public Either<ErrorMessage, String> getStatus() {
        return containerManager.getStatus(containerId);
    }

    public Either<ErrorMessage, String> killApp() {
        return containerManager.killContainer(containerId);
    }

    public Either<ErrorMessage, String> removeApp() {
        return containerManager.deleteContainer(containerId);
    }

    private Either<ErrorMessage, String> runApp(String imageId) {
        return runContainer(imageId, DockerImageManager.BuildType.RELEASE);
    }

    private Either<ErrorMessage, String> runAppTests(String imageId) {
        Either<ErrorMessage, String> runContainerResult = runContainer(imageId, DockerImageManager.BuildType.TEST);
        if (runContainerResult.isRight()) {
            String containerId = runContainerResult.get();
            Either<ErrorMessage, Long> waitForContainerResult = containerManager.waitForExit(containerId);

            if (waitForContainerResult.isRight()) {
                Long containerExitCode = waitForContainerResult.get();
                if (containerExitCode == 0) {
                    return Either.right("Tests passed!");
                } else {
                    Either<ErrorMessage, String> getLogs = containerManager.getLogs(containerId);
                    if (getLogs.isRight()) {
                        return Either.left(CoreErrorMessage.APP_TESTS_FAIL.withDetails(getLogs.get()));
                    } else {
                        return Either.left(getLogs.getLeft());
                    }
                }
            }
            return Either.left(waitForContainerResult.getLeft());
        }
        return runContainerResult;
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
        labels.put(PotapaasConfig.get("container_label_app_uuid"), appUuid);
        labels.put(PotapaasConfig.get("container_label_app_name"), requestDto.getName());
        labels.put(PotapaasConfig.get("container_label_app_git_repo_url"), requestDto.getSourceRepoUrl());
        labels.put(PotapaasConfig.get("container_label_app_git_repo_branch"), requestDto.getSourceBranchName());
        labels.put(PotapaasConfig.get("container_label_app_type"), requestDto.getType());
        labels.put(PotapaasConfig.get("container_label_build_type"), buildType.toString());

        ContainerConfig.Builder config = ContainerConfig.builder()
                .image(imageId)
                .exposedPorts(PotapaasConfig.get("default_webapp_port"))
                .hostConfig(hostConfig)
                .labels(labels);

        return containerManager.runContainer(config);
    }

    private Either<ErrorMessage, String> stopContainer(String containerId) {
        return containerManager.stopContainer(containerId, PotapaasConfig.getInt("container_stop_sec_to_wait_before_kill"));
    }

    private Either<ErrorMessage, String> buildImage(String appSourceDir, DockerImageManager.BuildType buildType) {
        return imageManager.buildImage(appSourceDir, buildType);
    }

    private Either<ErrorMessage, String> cloneRepo() {
        try {
            Path tmpTargetDir = Files.createTempDirectory(PotapaasConfig.get("tmp_git_dir_prefix"));
            return gitCloner.cloneBranch(requestDto.getSourceRepoUrl(), requestDto.getSourceBranchName(), tmpTargetDir.toString());
        } catch (Exception e) {
            return ExceptionMapper.map(e).of();
        }
    }
}
