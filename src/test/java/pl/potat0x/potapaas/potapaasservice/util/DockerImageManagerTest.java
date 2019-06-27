package pl.potat0x.potapaas.potapaasservice.util;

import io.vavr.control.Either;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerImageManagerTest {

    //todo: add helloworld source code to resources/test
    //todo: https://github.com/assertj/assertj-vavr

    @Test
    public void shouldBuildAndDeleteDockerImage() {
        DockerImageManager imageManager = new DockerImageManager("/home/ziemniak/programowanie/docker-test/expressjs");

        Either<String, String> result = imageManager.buildImage();
        String imageId = result.getOrElse("image build failed!");

        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.removeImage(imageId)).isEqualTo(Either.right(true));
        assertThat(imageManager.checkIfImageExists(imageId)).isEqualTo(Either.right(false));
    }
}
