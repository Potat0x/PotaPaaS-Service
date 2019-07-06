package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

public final class CaseBuilderStart {
    public static CaseBuilderEnd exception(Class<? extends Exception> exceptionClass) {
        return new CaseBuilderEnd(exceptionClass);
    }
}
