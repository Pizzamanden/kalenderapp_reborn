package com.example.kalenderapp_reborn.dataobjects;

public class TokenValidation {

    private String lastValidated;
    private String jsonWebToken;
    private int userID;
    private int validationStatus;
    // Status:
    // 0: in progress
    // 1: success
    // 2: failure

    public TokenValidation(String lastValidated, String jsonWebToken, int userID){
        this.lastValidated = lastValidated;
        this.jsonWebToken = jsonWebToken;
        this.userID = userID;
        this.validationStatus = 0;
    }

    public String getJsonWebToken() { return this.jsonWebToken; }
    public int getUserID() { return this.userID; }
    public int getValidationStatus() { return this.validationStatus; }
}
