package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.concurrent.Future;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class PostgresReadinessWaiter extends DatastoreReadinessWaiter {

    PostgresReadinessWaiter(long timeoutInMilliseconds) {
        super(timeoutInMilliseconds);
    }

    @Override
    public boolean checkIfTestRequestWorking(String address, int port, String username, String password) throws SQLException {
        try {
            Connection connection = createConnection(address, port, username, password);
            ResultSet resultSet = connection.createStatement().executeQuery("select 1337;");
            return resultSet.next() && resultSet.getInt(1) == 1337;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private Connection createConnection(String address, int port, String username, String password) throws TimeoutException, SQLException {
        return Future
                .of(() -> DriverManager.getConnection("jdbc:postgresql://" + address + ":" + port + "/postgres", username, password))
                .await(timeoutInMilliseconds, TimeUnit.MILLISECONDS)
                .get();
    }
}
