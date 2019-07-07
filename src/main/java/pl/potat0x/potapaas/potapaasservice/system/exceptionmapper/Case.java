package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import io.vavr.collection.List;
import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public final class Case {
    private final List<Class<? extends Exception>> exceptionClasses;
    private final ErrorMessage errorMessage;

    Case(ErrorMessage errorMessage, Class<? extends Exception>[] exceptionClasses) {
        this.errorMessage = errorMessage;
        this.exceptionClasses = List.of(exceptionClasses);
    }

    ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    List<Class<? extends Exception>> getExceptionClasses() {
        return exceptionClasses;
    }
}
