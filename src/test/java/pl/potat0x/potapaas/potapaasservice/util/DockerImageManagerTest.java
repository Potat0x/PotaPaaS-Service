package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerImageManagerTest {

    @Test
    public void shouldBuildAndDeleteDockerImage() {
        DockerImageManager imageManager = new DockerImageManager(PotapaasConfig.get("docker_api_uri"), getHelloworldAppSourceDir(), DockerImageManager.ImageType.NODEJS);

        for (var buildType : DockerImageManager.BuildType.values()) {
            String imageId = imageManager.buildImage(buildType).get();

            assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(true));
            assertThat(imageManager.removeImage(imageId)).isEqualTo(Either.right(true));
            assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(false));
        }
    }

    private String getHelloworldAppSourceDir() {
        return DockerImageManagerTest.class.getResource("/test/samples/nodejs/helloworld_app").getPath();
    }
}
