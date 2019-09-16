package pl.potat0x.potapaas.potapaasservice.system.errormessage;

public interface ErrorMessage {
    String getText();

    String getDetails();

    int getHttpStatus();
}
