package pl.potat0x.potapaas.potapaasservice.system.exceptionmapper;

public final class ErrorInfo {

    private final String message;
    private final int httpStatus;

    ErrorInfo(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public static ErrorInfo message(String message, int httpStatus) {
        return new ErrorInfo(message, httpStatus);
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return "ErrorInfo{" +
                "message='" + message + '\'' +
                ", httpStatus=" + httpStatus +
                '}';
    }
}
