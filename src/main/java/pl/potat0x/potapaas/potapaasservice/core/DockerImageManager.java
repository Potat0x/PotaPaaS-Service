package pl.potat0x.potapaas.potapaasservice.core;


import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import io.vavr.control.Either;
import org.springframework.util.FileSystemUtils;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final class DockerImageManager {

    enum BuildType {
        RELEASE,
        TEST
    }

    private final DockerClient docker;
    private final String imageTypeName;
    private final boolean buildingCacheEnabled;

    DockerImageManager(String dockerClientUri, AppType imageType) {
        this(dockerClientUri, imageType, false);
    }

    DockerImageManager(String dockerClientUri, AppType imageType, boolean buildingCacheEnabled) {
        docker = new DefaultDockerClient(dockerClientUri);
        imageTypeName = imageType.toString().toLowerCase();
        this.buildingCacheEnabled = buildingCacheEnabled;
    }

    public Either<ErrorMessage, String> buildImage(String applicationSrcDir, BuildType buildType) {
        try {
            Path temporaryBuildDir = createTempDirectory();
            Path appSourceCodeDir = Path.of(applicationSrcDir);

            copyAppSourcesToTempDirectory(appSourceCodeDir, temporaryBuildDir);
            copyDockerfileAndDockerignoreToTempDirectory(temporaryBuildDir, buildType);

            String imageId = buildDockerImage(temporaryBuildDir);
            deleteTempDirectory(temporaryBuildDir);
            return Either.right(imageId);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.DEPLOYMENT_ERROR);
        }
    }

    public Either<ErrorMessage, Boolean> removeImage(String imageId) {
        try {
            return Either.right(docker.removeImage(imageId).size() > 0);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    public Either<ErrorMessage, Boolean> checkIfImageExists(String imageId) {
        try {
            return Either.right(docker.listImages().stream().anyMatch(image -> image.id().startsWith("sha256:" + imageId)));
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    private String buildDockerImage(Path temporaryBuildDir) throws InterruptedException, DockerException, IOException {

        List<DockerClient.BuildParam> buildParams = new ArrayList<>();
        if (!buildingCacheEnabled) {
            buildParams.add(DockerClient.BuildParam.noCache());
        }

        return docker.build(temporaryBuildDir, buildParams.toArray(new DockerClient.BuildParam[0]));
    }

    private Path createTempDirectory() throws IOException {
        return Files.createTempDirectory(PotapaasConfig.get("tmp_image_building_dir_prefix") + imageTypeName + LocalDateTime.now());
    }

    private void copyAppSourcesToTempDirectory(Path applicationSrcDir, Path temporaryBuildDir) throws IOException {
        FileSystemUtils.copyRecursively(applicationSrcDir, temporaryBuildDir);
    }

    private void copyDockerfileAndDockerignoreToTempDirectory(Path temporaryBuildDir, BuildType buildType) throws IOException {
        Files.copy(getDockerfilePath(buildType), temporaryBuildDir.resolve("Dockerfile"));
        Files.copy(getDockerignorePath(), temporaryBuildDir.resolve(".dockerignore"));
    }

    private Path getFileFromResources(String filename) {
        return Path.of(DockerImageManager.class.getResource("/docker/" + imageTypeName + "/" + filename).getPath());
    }

    private Path getDockerfilePath(BuildType buildType) {
        String dockerfileLocation = buildType == BuildType.TEST ? "test/Dockerfile" : "release/Dockerfile";
        return getFileFromResources(dockerfileLocation);
    }

    private Path getDockerignorePath() {
        return getFileFromResources(".dockerignore");
    }

    private void deleteTempDirectory(Path temporaryBuildDir) throws IOException {
        FileSystemUtils.deleteRecursively(temporaryBuildDir);
    }
}
