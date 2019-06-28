package pl.potat0x.potapaas.potapaasservice.util;

import com.spotify.docker.client.messages.ContainerConfig;
import io.vavr.control.Either;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerContainerManagerTest {

    private DockerContainerManager manager = new DockerContainerManager("http://127.0.0.1:2375");

    @Test
    public void shouldRunAndKillContainer() {
        String containerId = manager.runContainer(defaultConfig()).get();

        assertThat(manager.checkIfContainerIsRunning(containerId)).isEqualTo(Either.right(true));
        assertThat(manager.killContainerIfRunning(containerId).get()).isTrue();
        assertThat(manager.checkIfContainerIsRunning(containerId).get()).isFalse();
    }

    @Test
    public void shouldConnectContainersToNetwork() {
        String containerId1 = manager.runContainer(defaultConfig()).get();
        String containerId2 = manager.runContainer(defaultConfig()).get();
        String networkId = manager.createNetwork().get();

        assertThat(manager.connectContainerToNetwork(containerId1, containerId2, networkId).get()).isTrue();
        assertThat(manager.checkIfContainersAreConnected(containerId1, containerId2, networkId).get()).isTrue();

        manager.killContainerIfRunning(containerId1);
        manager.killContainerIfRunning(containerId2);

        assertThat(manager.removeNetwork(networkId).get()).isTrue();
    }

    @Test
    public void shouldCreateLabeledContainersAndFindThemByLabel() {
        //given
        String containerId1 = manager.runContainer(
                defaultConfig().labels(Map.of("potapaas_label_test_1", "val_1"))
        ).get();

        String containerId2 = manager.runContainer(
                defaultConfig().labels(Map.of("potapaas_label_test_2", "val_2"))
        ).get();

        //when
        List<String> containerIdsWithLabel1 = manager.getContainersByLabel("potapaas_label_test_1", "val_1").get();
        List<String> containerIdsWithLabel2 = manager.getContainersByLabel("potapaas_label_test_2", "val_2").get();

        //then
        assertThat(containerIdsWithLabel1).containsExactly(containerId1);
        assertThat(containerIdsWithLabel2).containsExactly(containerId2);

        manager.killContainerIfRunning(containerId1);
        manager.killContainerIfRunning(containerId2);
    }

    private ContainerConfig.Builder defaultConfig() {
        String alpineImageId = "055936d3920576da37aa9bc460d70c5f212028bda1c08c0879aedf03d7a66ea1";
        Map<String, String> defaultLabels = Map.of("potapaas_test_container", "true");

        return ContainerConfig.builder()
                .image(alpineImageId)
                .cmd("sh", "-c", "sleep 1m")
                .labels(defaultLabels);
    }
}
