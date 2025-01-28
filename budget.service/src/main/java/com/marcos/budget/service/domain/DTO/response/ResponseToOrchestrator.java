package com.marcos.budget.service.domain.DTO.response;


public class ResponseToOrchestrator {

    private String message;
    private String action;

    public ResponseToOrchestrator() {
        this.message = "";
        this.action = "";
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
