package pl.potat0x.potapaas.potapaasservice.system.errormessage;

public interface ErrorMessage {
    String getText();

    int getHttpStatus();
}
