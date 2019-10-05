package pl.potat0x.potapaas.potapaasservice.datastore;

import io.vavr.control.Either;
import pl.potat0x.potapaas.potapaasservice.system.PotapaasConfig;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;
import pl.potat0x.potapaas.potapaasservice.utils.Clock;

import java.sql.SQLException;

import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

public abstract class DatastoreReadinessWaiter {

    protected final long timeoutInMilliseconds;

    abstract boolean checkIfTestRequestWorking(String address, int port, String username, String password) throws SQLException;

    protected DatastoreReadinessWaiter(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    protected Either<ErrorMessage, String> waitUntilDatastoreIsAvailable(String address, int port, String username, String password) {
        Clock timeoutClock = new Clock();
        do {
            try {
                Thread.sleep(PotapaasConfig.getInt("datastore_readiness_waiter_sleep_time_in_millis"));
                if (checkIfTestRequestWorking(address, port, username, password)) {
                    System.out.println("Datastore ready, time: " + timeoutClock.getElapsedTime() + " ms");
                    return Either.right("Datastore is ready");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Either.left(message("Waiting for datastore has been interrupted", 500));
            } catch (SQLException e) {
                System.out.print(".");
                //database is not yet ready: ignore exception and try again
            }
        } while (timeoutClock.getElapsedTime() < timeoutInMilliseconds);

        return Either.left(message("Waiting for datastore: timeout exceed", 500));
    }
}
