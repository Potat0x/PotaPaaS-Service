package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.CaseBuilderStart.exception;
import static pl.potat0x.potapaas.potapaasservice.system.exceptionmapper.ErrorInfo.message;

public class ExceptionMapperTest {
    @Test
    public void shouldMatchErrorInfoToException() {
        Exception e = new NullPointerException();

        ErrorInfo left = ExceptionMapper.map(e).of(
                exception(Exception.class).to(message("Exception", 500)),
                exception(NullPointerException.class).to(message("NPException", 500))
        ).getLeft();

        assertThat(left.getMessage()).isEqualTo("NPException");
    }

    @Test
    public void shouldReturnDefaultExceptionIfNoMatchesFound() {
        Exception e = new NullPointerException();

        ExceptionMapper.map(e).of().getLeft().getMessage();
        ExceptionMapper.map(e).of(exception(Exception.class).to(message("Exception", 418))).getLeft().getMessage();
        ExceptionMapper.map(e).of(exception(IOException.class).to(message("IOException", 418))).getLeft().getMessage();
    }
}
