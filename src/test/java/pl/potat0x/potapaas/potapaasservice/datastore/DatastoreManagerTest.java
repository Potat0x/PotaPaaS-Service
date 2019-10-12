package pl.potat0x.potapaas.potapaasservice.datastore;

import org.junit.Test;
import pl.potat0x.potapaas.potapaasservice.core.DockerContainerManager;
import pl.potat0x.potapaas.potapaasservice.core.DockerNetworkManager;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class DatastoreManagerTest {
    private final DockerContainerManager containerManager = new DockerContainerManager(PotapaasConfig.get("docker_api_uri"));

    @Test
    public void shouldStartPostgresDatastore() throws SQLException, InterruptedException {
        DatastoreReadinessWaiter datastoreReadinessWaiter = new SqlDatastoreReadinessWaiter(DatastoreType.POSTGRESQL, PotapaasConfig.getInt("datastore_startup_timeout_in_millis"));
        DockerNetworkManager networkManager = new DockerNetworkManager(PotapaasConfig.get("docker_api_uri"));
        DatastoreManager datastoreManager = new DatastoreManager(containerManager, DatastoreType.POSTGRESQL, networkManager, datastoreReadinessWaiter);
        String newDatastoreUuid = UUID.randomUUID().toString();

        datastoreManager.createAndStartDatastore(newDatastoreUuid);
        Connection connection = createPostgresConnection(datastoreManager.getPort().get());
        ResultSet resultSet = connection.createStatement().executeQuery("select 1234;");

        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1)).isEqualTo(1234);
    }

    private Connection createPostgresConnection(String port) throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:" + port + "/postgres", "postgres", "docker");
    }
}
