package pl.potat0x.potapaas.potapaasservice.system.errormessage;

public final class CustomErrorMessage implements ErrorMessage {

    private final String message;
    private final String details;
    private final int httpStatus;

    public static CustomErrorMessage message(String message, int httpStatus) {
        return new CustomErrorMessage(message, null, httpStatus);
    }

    private CustomErrorMessage(String message, String details, int httpStatus) {
        this.message = message;
        this.details = details;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getText() {
        return message;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return "CustomErrorMessage{" +
                "message='" + message + '\'' +
                ", httpStatus=" + httpStatus +
                '}';
    }
}
