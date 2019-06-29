package pl.potat0x.potapaas.potapaasservice.util;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.io.IOException;
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

    private DockerContainerManager containerManager = new DockerContainerManager(PotapaasConfig.get("docker_api_uri"));

    public AppDeployment(DeploymentType deploymentType, String githubRepoUrl, String branchName) {
        this.githubRepoUrl = githubRepoUrl;
        this.branchName = branchName;
        this.deploymentType = deploymentType;
        potapaasAppId = UUID.randomUUID().toString();
    }

    public Either<String, String> deployFromGithub() {
        return cloneRepo().flatMap(this::buildImage).flatMap(
                imageId -> runApp(imageId)
                        .map(containerId -> this.containerId = containerId)
                        .map(x -> potapaasAppId)
        );
    }

    public Try<Boolean> killApp() {
        return containerManager.killContainerIfRunning(containerId);
    }

    public Either<String, String> getPort() {
        return containerManager.getHostPort(containerId);
    }

    private Either<String, String> runApp(String imageId) {

        HostConfig hostConfig = HostConfig.builder()
                .publishAllPorts(true)
                .build();

        ContainerConfig.Builder config = ContainerConfig.builder()
                .image(imageId)
                .exposedPorts(PotapaasConfig.get("default_webapp_port"))
                .hostConfig(hostConfig)
                .labels(Map.of("potapaas_deployment", potapaasAppId.substring(0, 13) + "..."));

        return containerManager.runContainer(config);
    }

    private Either<String, String> buildImage(String appSourceDir) {
        DockerImageManager.ImageType dockerImageType = Match(deploymentType).of(
                Case($(DeploymentType.NODEJS), DockerImageManager.ImageType.NODEJS)
        );

        DockerImageManager imageManager = new DockerImageManager(PotapaasConfig.get("docker_api_uri"), appSourceDir, dockerImageType);
        return imageManager.buildImage();
    }

    private Either<String, String> cloneRepo() {
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory("potapaas_tmp_git");
        } catch (IOException e) {
            e.printStackTrace();
            return Either.left("create temp directory for github repo: " + e.getMessage());
        }
        GitCloner cloner = new GitCloner(tmpDir.toAbsolutePath().toString());
        return cloner.cloneBranch(githubRepoUrl, branchName);
    }
}
