package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerImageManagerTest {

    //todo: https://github.com/assertj/assertj-vavr

    @Test
    public void shouldBuildAndDeleteDockerImage() {
        DockerImageManager imageManager = new DockerImageManager("http://127.0.0.1:2375", getHelloworldAppSourceDir(), DockerImageManager.ImageType.NODEJS);

        Either<String, String> result = imageManager.buildImage();
        String imageId = result.getOrElse("image build failed!");

        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.removeImage(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(false));
    }

    private String getHelloworldAppSourceDir() {
        return DockerImageManagerTest.class.getResource("/test/samples/nodejs/helloworld_app").getPath();
    }
}
