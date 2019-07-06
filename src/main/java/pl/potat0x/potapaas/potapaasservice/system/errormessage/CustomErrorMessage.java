package pl.potat0x.potapaas.potapaasservice.system.errormessage;

public final class CustomErrorMessage implements ErrorMessage {

    private final String message;
    private final int httpStatus;

    public CustomErrorMessage(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public static CustomErrorMessage message(String message, int httpStatus) {
        return new CustomErrorMessage(message, httpStatus);
    }

    @Override
    public String getText() {
        return message;
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
