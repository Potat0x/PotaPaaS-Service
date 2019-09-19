package pl.potat0x.potapaas.potapaasservice.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(value = Error.class)
    public void errorHandler(Error e) {
        System.err.println("ErrorHandler:");
        e.printStackTrace();
        System.err.println("Terminating app...");
        System.exit(1);
    }
}
