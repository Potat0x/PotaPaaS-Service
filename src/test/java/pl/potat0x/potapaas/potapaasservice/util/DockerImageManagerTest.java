package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerImageManagerTest {

    @Test
    public void shouldBuildAndDeleteDockerImage() {
        DockerImageManager imageManager = new DockerImageManager("http://127.0.0.1:2375", getHelloworldAppSourceDir(), DockerImageManager.ImageType.NODEJS);

        String imageId = imageManager.buildImage().get();

        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.removeImage(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(false));
    }

    private String getHelloworldAppSourceDir() {
        return DockerImageManagerTest.class.getResource("/test/samples/nodejs/helloworld_app").getPath();
    }
}
