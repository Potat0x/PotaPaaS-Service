package pl.potat0x.potapaas.potapaasservice.util;


import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import io.vavr.control.Either;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static io.vavr.API.*;

final class DockerImageManager {

    public enum ImageType {
        NODEJS
    }

    private final Path applicationSrcDir;
    private final DockerClient docker;
    private final String imageTypeName;

    public DockerImageManager(String dockerClientUri, String applicationSrcDirectory, ImageType imageType) {
        docker = new DefaultDockerClient(dockerClientUri);
        applicationSrcDir = Path.of(applicationSrcDirectory);
        imageTypeName = Match(imageType).of(
                Case($(ImageType.NODEJS), "nodejs")
        );
    }

    public Either<String, String> buildImage() {
        try {
            Path temporaryBuildDir = createTempDirectory();
            copyAppSourcesToTempDirectory(temporaryBuildDir);
            copyDockerfileAndDockerignoreToTempDirectory(temporaryBuildDir);

            String imageId = buildDockerImage(temporaryBuildDir);
            deleteTempDirectory(temporaryBuildDir);
            return Either.right(imageId);
        } catch (Exception e) {
            e.printStackTrace();
            return Either.left(e.getMessage());
        }
    }

    public Either<String, Boolean> removeImage(String imageId) {
        try {
            return Either.right(docker.removeImage(imageId).size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            return Either.left(e.getMessage());
        }
    }

    public Either<String, Boolean> checkIfImageExists(String imageId) {
        try {
            return Either.right(docker.listImages().stream().anyMatch(image -> image.id().startsWith("sha256:" + imageId)));
        } catch (Exception e) {
            return Either.left(e.getMessage());
        }
    }

    private String buildDockerImage(Path temporaryBuildDir) throws InterruptedException, DockerException, IOException {
        return docker.build(temporaryBuildDir);
    }

    private Path createTempDirectory() throws IOException {
        return Files.createTempDirectory("potapaas_image_build_" + imageTypeName + LocalDateTime.now());
    }

    private void copyAppSourcesToTempDirectory(Path temporaryBuildDir) throws IOException {
        FileSystemUtils.copyRecursively(applicationSrcDir, temporaryBuildDir);
    }

    private void copyDockerfileAndDockerignoreToTempDirectory(Path temporaryBuildDir) throws IOException {
        Files.copy(getDockerfilePath(), temporaryBuildDir.resolve("Dockerfile"));
        Files.copy(getDockerignorePath(), temporaryBuildDir.resolve(".dockerignore"));
    }

    private Path getFileFromResources(String filename) {
        return Path.of(DockerImageManager.class.getResource("/docker/" + imageTypeName + "/" + filename).getPath());
    }

    private Path getDockerfilePath() {
        return getFileFromResources("Dockerfile");
    }

    private Path getDockerignorePath() {
        return getFileFromResources(".dockerignore");
    }

    private void deleteTempDirectory(Path temporaryBuildDir) throws IOException {
        FileSystemUtils.deleteRecursively(temporaryBuildDir);
    }
}
