package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

public final class CaseBuilderStart {
    @SafeVarargs
    public static CaseBuilderEnd exception(Class<? extends Exception>... exceptionClasses) {
        return new CaseBuilderEnd(exceptionClasses);
    }
}
