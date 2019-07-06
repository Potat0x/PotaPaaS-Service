package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

public final class CaseBuilderEnd {
    private final Class<? extends Exception> exceptionClass;

    CaseBuilderEnd(Class<? extends Exception> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public Case to(ErrorInfo errorInfo) {
        return new Case(errorInfo, exceptionClass);
    }
}
