package com.example.kalenderapp_reborn.dataobjects;

public class SignupQuery {


    private SignupRequest signupRequest;
    private SignupResponse signupResponse;

    public SignupQuery(String email, String firstname, String password){
        this.signupRequest = new SignupRequest(email, firstname, password);
    }

    public SignupResponse getSignupResponse(){
        return this.signupResponse;
    }





    private class SignupRequest {

        private String email;
        private String firstname;
        private String password;

        private SignupRequest(String email, String firstname, String password){
            this.email = email;
            this.firstname = firstname;
            this.password = password;
        }

    }




    private class SignupResponse{

        private int responseCode;
        private String responseString;
        private TokenValidation tokenValidation;

        public int getResponseCode(){
            return this.responseCode;
        }
        public String getResponseString(){
            return this.responseString;
        }
        public TokenValidation getTokenValidation(){
            return this.tokenValidation;
        }

    }

}
