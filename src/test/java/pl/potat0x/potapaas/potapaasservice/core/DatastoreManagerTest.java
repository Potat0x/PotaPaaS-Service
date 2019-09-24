package pl.potat0x.potapaas.potapaasservice.core;

import org.junit.Test;
import pl.potat0x.potapaas.potapaasservice.datastore.DatastoreType;
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
        DatastoreManager datastoreManager = new DatastoreManager(containerManager, DatastoreType.POSTGRES, new DockerNetworkManager(PotapaasConfig.get("docker_api_uri")));
        datastoreManager.createAndStartDatastore(UUID.randomUUID().toString());

        Thread.sleep(PotapaasConfig.getInt("database_startup_waiting_time_in_millis"));
        Connection connection = createPostgresConnection(datastoreManager.getPort().get());
        ResultSet resultSet = connection.createStatement().executeQuery("select 1234;");

        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1)).isEqualTo(1234);
    }

    private Connection createPostgresConnection(String port) throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:" + port + "/postgres", "postgres", "docker");
    }
}
