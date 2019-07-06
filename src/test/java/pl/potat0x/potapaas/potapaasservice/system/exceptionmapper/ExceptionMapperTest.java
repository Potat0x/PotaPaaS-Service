package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import org.junit.Test;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.CaseBuilderStart.exception;
import static pl.potat0x.potapaas.potapaasservice.system.errormessage.CustomErrorMessage.message;

public class ExceptionMapperTest {
    @Test
    public void shouldMatchErrorInfoToException() {
        Exception e = new NullPointerException();

        ErrorMessage errorMessage = ExceptionMapper.map(e).of(
                exception(Exception.class).to(message("Exception", 500)),
                exception(NullPointerException.class).to(message("NPException", 500))
        ).getLeft();

        assertThat(errorMessage.getText()).isEqualTo("NPException");
    }

    @Test
    public void shouldReturnDefaultExceptionIfNoMatchesFound() {
        Exception e = new NullPointerException();

        ExceptionMapper.map(e).of().getLeft().getText();
        ExceptionMapper.map(e).of(exception(Exception.class).to(message("Exception", 418))).getLeft().getText();
        ExceptionMapper.map(e).of(exception(IOException.class).to(message("IOException", 418))).getLeft().getText();
    }

    @Test
    public void shouldMapAnyExceptionToGivenErrorInfo() {
        ErrorMessage errorMessage = ExceptionMapper.map(new NullPointerException())
                .to(message("errormessage 1", 418)).getLeft();
        ErrorMessage errorInfo2 = ExceptionMapper.map(new IOException())
                .to(message("errormessage 2", 418)).getLeft();

        assertThat(errorMessage.getText()).isEqualTo("errormessage 1");
        assertThat(errorInfo2.getText()).isEqualTo("errormessage 2");
    }
}
