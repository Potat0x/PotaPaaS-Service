package pl.potat0x.potapaas.potapaasservice.core;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import io.vavr.control.Either;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerContainerManagerTest {

    private DockerContainerManager manager = new DockerContainerManager(PotapaasConfig.get("docker_api_uri"));
    private static String testImageName = "alpine:latest";

    @BeforeClass
    public static void pullAlpineImage() throws DockerException, InterruptedException {
        DefaultDockerClient docker = new DefaultDockerClient(PotapaasConfig.get("docker_api_uri"));
        if (docker.listImages(DockerClient.ListImagesParam.byName(testImageName)).size() == 0) {
            docker.pull(testImageName);
        }
    }

    @Test
    public void shouldRunAndKillContainer() {
        String containerId = manager.runContainer(defaultConfig()).get();

        assertThat(manager.checkIfContainerIsRunning(containerId)).isEqualTo(Either.right(true));
        assertThat(manager.killContainer(containerId).isSuccess()).isTrue();
        assertThat(manager.checkIfContainerIsRunning(containerId).get()).isFalse();
    }

    @Test
    public void shouldConnectContainersToNetwork() {
        String containerId1 = manager.runContainer(defaultConfig()).get();
        String containerId2 = manager.runContainer(defaultConfig()).get();
        String networkId = manager.createNetwork().get();

        assertThat(manager.connectContainerToNetwork(containerId1, containerId2, networkId).isSuccess()).isTrue();
        assertThat(manager.checkIfContainersAreConnected(containerId1, containerId2, networkId).get()).isTrue();

        manager.killContainer(containerId1);
        manager.killContainer(containerId2);

        assertThat(manager.removeNetwork(networkId).isSuccess()).isTrue();
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

        manager.killContainer(containerId1);
        manager.killContainer(containerId2);
    }

    @Test
    public void shouldReadContainerLogs() {
        String logContent = "container log test";
        ContainerConfig.Builder config = defaultConfig()
                .cmd("echo", logContent);

        String containerId = manager.runContainer(config).get();
        String logs = manager.getLogs(containerId).get();

        assertThat(logs).contains(logContent);
    }

    private ContainerConfig.Builder defaultConfig() {
        Map<String, String> defaultLabels = Map.of("potapaas_test_container", "true");

        return ContainerConfig.builder()
                .image(testImageName)
                .cmd("sh", "-c", "sleep 1m")
                .labels(defaultLabels);
    }
}
