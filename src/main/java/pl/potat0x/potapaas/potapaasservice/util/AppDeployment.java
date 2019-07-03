package pl.potat0x.potapaas.potapaasservice.util;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static io.vavr.API.*;

final class AppDeployment {

    public enum DeploymentType {
        NODEJS("Node.js (NPM)");

        public final String value;

        DeploymentType(String value) {
            this.value = value;
        }
    }

    private final String githubRepoUrl;
    private final String branchName;
    private final DeploymentType deploymentType;
    private final String potapaasAppId;
    private String containerId;
    private String appSourceDir;

    private final DockerContainerManager containerManager;

    public AppDeployment(DeploymentType deploymentType, String githubRepoUrl, String branchName) {
        containerManager = new DockerContainerManager(PotapaasConfig.get("docker_api_uri"));
        potapaasAppId = UUID.randomUUID().toString();
        this.githubRepoUrl = githubRepoUrl;
        this.branchName = branchName;
        this.deploymentType = deploymentType;
    }

    public Either<String, String> deploy() {
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

    public Try<Void> killApp() {
        return containerManager.killContainer(containerId);
    }

    public Either<String, String> getPort() {
        return containerManager.getHostPort(containerId);
    }

    public Either<String, String> getLogs() {
        return containerManager.getLogs(containerId);
    }

    private Either<String, String> runApp(String imageId) {
        return runContainer(imageId, DockerImageManager.BuildType.RELEASE);
    }

    private Either<String, String> runAppTests(String imageId) {
        return runContainer(imageId, DockerImageManager.BuildType.TEST)
                .flatMap(containerManager::waitForExit)
                .flatMap(exitCode -> exitCode == 0 ? Either.right("Tests passed!") : Either.left("Tests failed!"));
    }

    private Either<String, String> buildReleaseImage() {
        return buildImage(appSourceDir, DockerImageManager.BuildType.RELEASE);
    }

    private Either<String, String> buildTestImage() {
        return buildImage(appSourceDir, DockerImageManager.BuildType.TEST);
    }

    private Either<String, String> runContainer(String imageId, DockerImageManager.BuildType buildType) {
        HostConfig hostConfig = HostConfig.builder()
                .publishAllPorts(buildType == DockerImageManager.BuildType.RELEASE)
                .build();

        Map<String, String> labels = Map.of("potapaas_deployment_" + buildType, potapaasAppId.substring(0, 13) + "...");
        ContainerConfig.Builder config = ContainerConfig.builder()
                .image(imageId)
                .exposedPorts(PotapaasConfig.get("default_webapp_port"))
                .hostConfig(hostConfig)
                .labels(labels);

        return containerManager.runContainer(config);
    }

    private Either<String, String> buildImage(String appSourceDir, DockerImageManager.BuildType buildType) {
        DockerImageManager.ImageType dockerImageType = Match(deploymentType).of(
                Case($(DeploymentType.NODEJS), DockerImageManager.ImageType.NODEJS)
        );

        DockerImageManager imageManager = new DockerImageManager(PotapaasConfig.get("docker_api_uri"), appSourceDir, dockerImageType);
        return imageManager.buildImage(buildType);
    }

    private Either<String, String> cloneRepo() {
        try {
            Path tmpDir = Files.createTempDirectory("potapaas_tmp_git");
            return GitCloner.create(tmpDir.toAbsolutePath())
                    .flatMap(cloner -> cloner.cloneBranch(githubRepoUrl, branchName));
        } catch (Exception e) {
            e.printStackTrace();
            return Either.left("create temp directory for github repo: " + e.getMessage());
        }
    }
}
