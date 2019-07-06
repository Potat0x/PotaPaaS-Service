package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public final class Case {
    private final Class<? extends Exception> exceptionClass;
    private final ErrorMessage errorMessage;

    Case(ErrorMessage errorMessage, Class<? extends Exception> exceptionClass) {
        this.errorMessage = errorMessage;
        this.exceptionClass = exceptionClass;
    }

    ErrorMessage getErrorInfo() {
        return errorMessage;
    }

    Class<? extends Exception> getExceptionClass() {
        return exceptionClass;
    }
}
