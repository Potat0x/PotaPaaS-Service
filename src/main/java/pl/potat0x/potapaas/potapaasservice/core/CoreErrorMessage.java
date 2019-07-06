package pl.potat0x.potapaas.potapaasservice.core;

import pl.potat0x.potapaas.potapaasservice.system.errormessage.ErrorMessage;

enum CoreErrorMessage implements ErrorMessage {
    SERVER_ERROR("Server errormessage", 500),
    DEPLOYMENT_ERROR("Error while creating deployment", 500),
    GIT_CLONE_ERROR("Error while creating deployment", 424),
    APP_TESTS_FAIL("Application tests failed", 200),
    APP_START_FAIL("Application start failed", 500),
    INSPECT_CONTAINER("Container inspection failed", 500),
    CONTAINER_NOT_FOUND("Application not found", 404),
    CONTAINER_INSPECTION_FAILED("Error while inspecting container", 500);

    private String text;
    private int httpStatus;

    CoreErrorMessage(String text, int httpStatus) {
        this.text = text;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}
