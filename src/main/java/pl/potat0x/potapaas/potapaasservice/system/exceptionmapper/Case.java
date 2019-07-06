package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

public final class Case {
    private final Class<? extends Exception> exceptionClass;
    private final ErrorInfo errorInfo;

    Case(ErrorInfo errorInfo, Class<? extends Exception> exceptionClass) {
        this.errorInfo = errorInfo;
        this.exceptionClass = exceptionClass;
    }

    ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    Class<? extends Exception> getExceptionClass() {
        return exceptionClass;
    }
}
