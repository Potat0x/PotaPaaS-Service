package pl.potat0x.potapaas.potapaasservice.core;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.BadParamException;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.PortBinding;
import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ExceptionMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;
import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.CaseBuilderStart.exception;

public final class DockerContainerManager {

    private final DockerClient docker;

    public DockerContainerManager(String dockerClientUri) {
        docker = new DefaultDockerClient(dockerClientUri);
    }

    Either<ErrorMessage, String> runContainer(ContainerConfig.Builder containerConfig) {
        try {
            ContainerCreation containerCreation = docker.createContainer(containerConfig.build());
            docker.startContainer(containerCreation.id());
            return Either.right(containerCreation.id());
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.APP_START_FAIL);
        }
    }

    Either<ErrorMessage, String> getHostPort(String containerId) {
        try {
            ImmutableMap<String, List<PortBinding>> ports = docker.inspectContainer(containerId).networkSettings().ports();
            if (ports == null || ports.isEmpty()) {
                return Either.left(message("no port bindings found", 500));
            } else {
                if (ports.containsKey(PotapaasConfig.get("default_webapp_port"))) {
                    return Either.right(ports.get(PotapaasConfig.get("default_webapp_port")).get(0).hostPort());
                } else {
                    return Either.right(ports.get(PotapaasConfig.get("default_datastore_port")).get(0).hostPort());
                }
            }
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, Boolean> checkIfContainerIsRunning(String containerId) {
        try {
            return Either.right(docker.inspectContainer(containerId).state().running());
        } catch (Exception e) {
            return ExceptionMapper.map(e).of(
                    exception(ContainerNotFoundException.class).to(CoreErrorMessage.CONTAINER_NOT_FOUND),
                    exception(DockerException.class).to(CoreErrorMessage.SERVER_ERROR)
            );
        }
    }

    Either<ErrorMessage, String> killContainer(String containerId) {
        try {
            docker.killContainer(containerId);
            return Either.right(containerId);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, String> stopContainer(String containerId, int secondsToWaitBeforeKilling) {
        try {
            docker.stopContainer(containerId, secondsToWaitBeforeKilling);
            return Either.right(containerId);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, String> deleteContainer(String containerId) {
        try {
            docker.removeContainer(containerId);
            return Either.right(containerId);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, Long> waitForExit(String containerId) {
        try {
            while (true) {
                if (!docker.inspectContainer(containerId).state().running()) {
                    return Either.right(docker.inspectContainer(containerId).state().exitCode());
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            return ExceptionMapper.map(e).of(
                    exception(ContainerNotFoundException.class).to(CoreErrorMessage.CONTAINER_NOT_FOUND),
                    exception(DockerException.class).to(CoreErrorMessage.SERVER_ERROR)
            );
        }
    }

    Either<ErrorMessage, String> connectContainerToNetwork(String containerId1, String containerId2, String networkId) {
        try {
            docker.connectToNetwork(containerId1, networkId);
            docker.connectToNetwork(containerId2, networkId);
            return Either.right(networkId);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, Boolean> checkIfContainersAreConnected(String containerId1, String containerId2, String networkId) {
        try {
            return Either.right(checkIfContainerIsConnectedToNetwork(containerId1, networkId)
                    && checkIfContainerIsConnectedToNetwork(containerId2, networkId));
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, String> createNetwork() {
        NetworkConfig networkConfig = NetworkConfig.builder()
                .attachable(true)
                .name(PotapaasConfig.get("network_name_prefix") + UUID.randomUUID())
                .checkDuplicate(true)
                .build();
        try {
            return Either.right(docker.createNetwork(networkConfig).id());
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, String> removeNetwork(String networkId) {
        try {
            docker.removeNetwork(networkId);
            return Either.right(networkId);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, List<String>> getContainersByLabel(String label, String value) {
        try {
            List<String> containerIds = docker.listContainers(DockerClient.ListContainersParam.withLabel(label, value))
                    .stream()
                    .map(Container::id)
                    .collect(Collectors.toList());
            return Either.right(containerIds);
        } catch (Exception e) {
            return ExceptionMapper.map(e).to(CoreErrorMessage.SERVER_ERROR);
        }
    }

    Either<ErrorMessage, String> getLogs(String containerId) {

        DockerClient.LogsParam[] logsParams = new DockerClient.LogsParam[]{
                DockerClient.LogsParam.timestamps(),
                DockerClient.LogsParam.stderr(),
                DockerClient.LogsParam.stdout()
        };

        try {
            String logs = docker.logs(containerId, logsParams).readFully();
            return Either.right(logs);
        } catch (Exception e) {
            return ExceptionMapper.map(e).of(
                    exception(ContainerNotFoundException.class).to(CoreErrorMessage.CONTAINER_NOT_FOUND),
                    exception(DockerException.class, BadParamException.class).to(CoreErrorMessage.SERVER_ERROR)
            );
        }
    }

    Either<ErrorMessage, String> getStatus(String containerId) {
        try {
            ContainerState state = docker.inspectContainer(containerId).state();
            return Either.right(state.status());
        } catch (Exception e) {
            return ExceptionMapper.map(e).of(
                    exception(ContainerNotFoundException.class).to(CoreErrorMessage.CONTAINER_NOT_FOUND),
                    exception(DockerException.class).to(CoreErrorMessage.SERVER_ERROR)
            );
        }
    }

    Either<ErrorMessage, LocalDateTime> getCreationDate(String containerId) {
        try {
            Date creationDate = docker.inspectContainer(containerId).created();
            LocalDateTime creationDateTime = LocalDateTime.ofInstant(creationDate.toInstant(), ZoneId.systemDefault());
            return Either.right(creationDateTime);
        } catch (Exception e) {
            return ExceptionMapper.map(e).of(
                    exception(ContainerNotFoundException.class).to(CoreErrorMessage.CONTAINER_NOT_FOUND),
                    exception(DockerException.class).to(CoreErrorMessage.SERVER_ERROR)
            );
        }
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
