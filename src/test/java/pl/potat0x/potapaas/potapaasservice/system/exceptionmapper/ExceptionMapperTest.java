package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import org.junit.Test;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;
import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.CaseBuilderStart.exception;

public class ExceptionMapperTest {

    private static final Exception e = new NullPointerException();

    @Test
    public void shouldMapExceptionToErrorMessage() {
        ErrorMessage errorMessage = ExceptionMapper.map(e).of(
                exception(Exception.class).to(message("Exception message", 500)),
                exception(NullPointerException.class).to(message("NPException message", 500)),
                exception(RuntimeException.class, IOException.class).to(message("another exceptions message", 500))
        ).getLeft();

        assertThat(errorMessage.getText()).isEqualTo("NPException message");
    }

    @Test
    public void shouldMapAnyExceptionToGivenErrorMessage() {
        ErrorMessage errorMessage = ExceptionMapper
                .map(e).to(message("message", 418)).getLeft();

        assertThat(errorMessage.getText()).isEqualTo("message");
    }

    @Test
    public void shouldReturnDefaultErrorMessageIfNoMappingFound() {
        ExceptionMapper.map(e).of().getLeft().getText();

        String unexpectedMessage = "unexpected message";

        String message = ExceptionMapper.map(e).of(
                exception().to(message(unexpectedMessage, 418))
        ).getLeft().getText();

        String message2 = ExceptionMapper.map(e).of(
                exception(Exception.class).to(message(unexpectedMessage, 418))
        ).getLeft().getText();

        String message3 = ExceptionMapper.map(e).of(
                exception(IOException.class, InterruptedException.class, RuntimeException.class).to(message("message", 418))
        ).getLeft().getText();


        assertThat(message).isNotEqualTo(unexpectedMessage);
        assertThat(message2).isNotEqualTo(unexpectedMessage);
        assertThat(message3).isNotEqualTo(unexpectedMessage);
    }

    @Test
    public void shouldMapMultipleExceptionsToSameErrorMessage() {
        String messageText = ExceptionMapper.map(e).of(
                exception(Exception.class, IOException.class, NullPointerException.class, IllegalStateException.class)
                        .to(message("message 1", 418)),
                exception(InterruptedException.class)
                        .to(message("message 2", 418)),
                exception(RuntimeException.class, ArithmeticException.class)
                        .to(message("message 3", 418))
        ).getLeft().getText();

        assertThat(messageText).isEqualTo("message 1");
    }
}
