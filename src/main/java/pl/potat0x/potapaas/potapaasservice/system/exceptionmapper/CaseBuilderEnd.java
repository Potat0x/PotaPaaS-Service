package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public final class CaseBuilderEnd {
    private final Class<? extends Exception> exceptionClass;

    CaseBuilderEnd(Class<? extends Exception> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public Case to(ErrorMessage errorMessage) {
        return new Case(errorMessage, exceptionClass);
    }
}
