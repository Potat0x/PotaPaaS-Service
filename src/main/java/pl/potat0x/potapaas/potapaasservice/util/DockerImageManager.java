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

final class DockerImageManager {

    private final Path dockerfilePath;
    private final Path applicationSrcDir;
    private final DockerClient docker;

    public DockerImageManager(String applicationSrcDirectory) {
        docker = new DefaultDockerClient("http://127.0.0.1:2375");
        applicationSrcDir = Path.of(applicationSrcDirectory);
        dockerfilePath = getDockerfilePath();
    }

    public Either<String, String> buildImage() {
        try {
            Path temporaryBuildDir = createTempDirectory();
            copyAppSourcesToTempDirectory(temporaryBuildDir);
            copyDockerfileToTempDirectory(temporaryBuildDir);
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

    private Path getDockerfilePath() {
        return Path.of(DockerImageManager.class.getResource("/test/dockerfiles/NodeJS").getPath());
    }

    private Path createTempDirectory() throws IOException {
        return Files.createTempDirectory("potapaas_image_build_nodejs" + LocalDateTime.now());
    }

    private void copyAppSourcesToTempDirectory(Path temporaryBuildDir) throws IOException {
        FileSystemUtils.copyRecursively(applicationSrcDir, temporaryBuildDir);
    }

    private void copyDockerfileToTempDirectory(Path temporaryBuildDir) throws IOException {
        Files.copy(dockerfilePath, temporaryBuildDir.resolve("Dockerfile"));
    }

    private void deleteTempDirectory(Path temporaryBuildDir) throws IOException {
        FileSystemUtils.deleteRecursively(temporaryBuildDir);
    }
}
