package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

public final class CaseBuilderEnd {
    private final Class<? extends Exception>[] exceptionClasses;

    CaseBuilderEnd(Class<? extends Exception>[] exceptionClasses) {
        this.exceptionClasses = exceptionClasses;
    }

    public Case to(ErrorMessage errorMessage) {
        return new Case(errorMessage, exceptionClasses);
    }
}
