package com.example.kalenderapp_reborn.dataobjects;

public class TokenValidation {

    private String lastValidated;
    private String jsonWebToken;
    private int validationStatus;
    private String validationMessage;
    // Status:
    // 0: in progress
    // 1: success
    // 2: failure

    public TokenValidation(String lastValidated, String jsonWebToken){
        this.lastValidated = lastValidated;
        this.jsonWebToken = jsonWebToken;
        this.validationStatus = 0;
    }

    public String getJsonWebToken() { return this.jsonWebToken; }
    public int getValidationStatus() { return this.validationStatus; }
    public String getValidationMessage() { return this.validationMessage; }
}
