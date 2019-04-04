package com.example.kalenderapp_reborn.dataobjects;

public class DataJsonSQLSelect {

    private String request;
    private String userToken;
    private DataJsonIdentifiers identifiers;

    public DataJsonSQLSelect(String request, String userToken){
        this.request = request;
        this.userToken = userToken;
    }

    // Getters
    public String getRequest(){ return this.request; }
    public String getUserToken(){ return this.userToken; }
    public DataJsonIdentifiers getIdentifiers() { return this.identifiers; }

    // Setters
    public void setIdentifiers(DataJsonIdentifiers identifiers) { this.identifiers = identifiers; }


    // Inner class
    public class DataJsonIdentifiers {

        private int userId;
        private String homeAddress;

        public DataJsonIdentifiers(int userId){
            this.userId = userId;
        }

        public DataJsonIdentifiers(int userId, String homeAddress){
            this.userId = userId;
            this.homeAddress = homeAddress;
        }


        // Getters
        public int getUserId() { return this.userId; }
        public String getHomeAdress() { return this.homeAddress; }
    }
}