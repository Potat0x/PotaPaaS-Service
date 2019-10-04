package pl.potat0x.potapaas.potapaasservice.datastore

import io.vavr.control.Either
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage
import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

class PostgresReadinessWaiterTest extends Specification {

    private static int invalidUnusedPort
    private static int invalidPortUsedByAnotherApp
    private static ServerSocket portAllocSocket

    def "should get timeout error while connecting to nonexistent database"() {
        given:
        int testTimeout = 100
        PostgresReadinessWaiter postgresReadinessWaiter = new PostgresReadinessWaiter(testTimeout)

        when:
        Either<ErrorMessage, String> waitingResult = postgresReadinessWaiter.waitUntilDatastoreIsAvailable("127.0.0.1", port, "postgres", "docker")

        then:
        assertThat(waitingResult.isLeft()).isTrue()
        assertThat(waitingResult.getLeft().getText()).contains("timeout")

        where:
        port << [
                invalidUnusedPort,
                invalidPortUsedByAnotherApp
        ]
    }

    def setupSpec() {
        portAllocSocket = new ServerSocket(0)
        invalidPortUsedByAnotherApp = portAllocSocket.getLocalPort()

        def tempSocket = new ServerSocket(0)
        invalidUnusedPort = tempSocket.getLocalPort()
        tempSocket.close()
    }

    def cleanupSpec() {
        portAllocSocket.close()
    }
}
