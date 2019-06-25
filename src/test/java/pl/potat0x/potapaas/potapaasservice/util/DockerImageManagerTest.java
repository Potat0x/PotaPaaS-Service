package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerImageManagerTest {

    @Test
    public void shouldBuildAndDeleteDockerImage() {
        DockerImageManager imageManager = new DockerImageManager("/home/ziemniak/programowanie/docker-test/expressjs");//todo: add helloworld app to resources/test

        Either<String, String> e = imageManager.buildImage();
        String imageId = e.getOrElse("image build failed!");

        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.removeImage(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(false));
    }
}
