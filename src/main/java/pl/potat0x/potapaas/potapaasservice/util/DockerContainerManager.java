package pl.potat0x.potapaas.potapaasservice.util;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.PortBinding;
import io.vavr.control.Either;
import io.vavr.control.Try;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

final class DockerContainerManager {

    private final DockerClient docker;

    public DockerContainerManager(String dockerClientUri) {
        docker = new DefaultDockerClient(dockerClientUri);
    }

    public Either<String, String> runContainer(ContainerConfig.Builder containerConfig) {
        try {
            ContainerCreation containerCreation = docker.createContainer(containerConfig.build());
            docker.startContainer(containerCreation.id());
            return Either.right(containerCreation.id());
        } catch (Exception e) {
            e.printStackTrace();
            return Either.left(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    public Either<String, String> getHostPort(String containerId) {
        try {
            ImmutableMap<String, List<PortBinding>> ports = docker.inspectContainer(containerId).networkSettings().ports();
            if (ports == null || ports.isEmpty()) {
                return Either.left("no port bindings found");
            } else {
                return Either.right(ports.get(PotapaasConfig.get("default_webapp_port")).get(0).hostPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Either.left(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    public Either<String, Boolean> checkIfContainerIsRunning(String containerId) {
        try {
            return Either.right(docker.inspectContainer(containerId).state().running());
        } catch (Exception e) {
            e.printStackTrace();
            return Either.left(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    public Try<Boolean> killContainerIfRunning(String containerId) {
        return Try.of(() -> {
            docker.killContainer(containerId);
            return true;
        });
    }

    public Try<Boolean> connectContainerToNetwork(String containerId1, String containerId2, String networkId) {
        return Try.of(() -> {
            docker.connectToNetwork(containerId1, networkId);
            docker.connectToNetwork(containerId2, networkId);
            return true;
        });
    }

    public Try<Boolean> checkIfContainersAreConnected(String containerId1, String containerId2, String networkId) {
        return Try.of(() -> checkIfContainerIsConnectedToNetwork(containerId1, networkId)
                && checkIfContainerIsConnectedToNetwork(containerId2, networkId));
    }

    public Try<String> createNetwork() {
        NetworkConfig networkConfig = NetworkConfig.builder()
                .attachable(true)
                .name("potapaas_test_network_" + UUID.randomUUID())
                .checkDuplicate(true)
                .build();

        return Try.of(() -> docker.createNetwork(networkConfig).id());
    }

    public Try<Boolean> removeNetwork(String networkId) {
        return Try.of(() -> {
            docker.removeNetwork(networkId);
            return true;
        });
    }

    public Try<List<String>> getContainersByLabel(String label, String value) {
        return Try.of(() ->
                docker.listContainers(DockerClient.ListContainersParam.withLabel(label, value))
                        .stream()
                        .map(Container::id)
                        .collect(Collectors.toList())
        );
    }

    private Set<String> getNetworkIds(String containerId) throws DockerException, InterruptedException {
        Map<String, AttachedNetwork> containerNetworks = docker.inspectContainer(containerId).networkSettings().networks();

        if (containerNetworks != null) {
            return containerNetworks.values().stream()
                    .map(AttachedNetwork::networkId)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private boolean checkIfContainerIsConnectedToNetwork(String containerId, String networkId) throws DockerException, InterruptedException {
        return getNetworkIds(containerId).contains(networkId);
    }
}