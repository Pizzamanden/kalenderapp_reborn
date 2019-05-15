package com.example.kalenderapp_reborn.dataobjects;

public class LoginQuery {

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    public LoginQuery(String email, String password){
        this.loginRequest = new LoginRequest(email, password);
    }

    public TokenValidation getTokenValidation(){
        return loginResponse.getTokenValidation();
    }
    public int getResponseCode(){
        return loginResponse.getResponseCode();
    }
    public String getResponseMessage(){
        return loginResponse.getResponseMessage();
    }

    private class LoginRequest{

        private String email;
        private String password;

        private LoginRequest(String email, String password){
            this.email = email;
            this.password = password;
        }

    }
    private class LoginResponse{

        private TokenValidation tokenValidation;
        private int responseCode;
        private String responseMessage;

        private TokenValidation getTokenValidation() {
            return tokenValidation;
        }
        private int getResponseCode() {
            return responseCode;
        }
        private String getResponseMessage() {
            return responseMessage;
        }

    }
}
