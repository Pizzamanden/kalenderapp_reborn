package com.example.kalenderapp_reborn.dataobjects;

import java.util.ArrayList;



/*

    Welcome to my SQL Gson file
    In here, i attempt to make a class that takes a construct when build, or contains getters when being mapped to
    Hopefully, this class can handle all my SQL queries that i make

    What i want to send to the server:
    -The users token
    -The users ID
    -A query type
    -Maybe a table row (insert)
    -Maybe an ID' for a table row (select and/or delete)
    -Maybe both of the above (update)

    What i want back:
    -The users ID
    -A Response code and/or a message
    -Maybe a row
    -Maybe more than one row

 */







public class SQLQueryJson {

    // Fields
    private String token;
    private int userID;
    private QueryRequest queryRequest;
    private QueryResponse queryResponse;

    /**
     * This is for:
     * Insertions
     *
     * @param token The users token
     * @param dataJsonCalendarEntries Object, styled as a row from CalendarEntries DB
     * @param queryType What type of query to perform
     * @param userID The users ID
     */
    public SQLQueryJson(String token, DataJsonCalendarEntries dataJsonCalendarEntries, String queryType, int userID){
        this.token = token;
        this.userID = userID;
        this.queryRequest = new QueryRequest(dataJsonCalendarEntries, queryType);
    }
    /**
     * This is for:
     * Updates
     *
     * @param token The users token
     * @param dataJsonCalendarEntries Object, styled as a row from CalendarEntries DB
     * @param queryType What type of query to perform
     * @param userID The users ID
     * @param entryID The ID of the entry
     */
    public SQLQueryJson(String token, DataJsonCalendarEntries dataJsonCalendarEntries, String queryType, int userID, int entryID){
        this.token = token;
        this.userID = userID;
        this.queryRequest = new QueryRequest(dataJsonCalendarEntries, queryType, entryID);
    }
    /**
     * This is for:
     * Selections, singular
     * Deletions
     *
     * @param token The users token
     * @param queryType What type of query to perform
     * @param userID The users ID
     * @param entryID The ID of the entry
     */
    public SQLQueryJson(String token, String queryType, int userID, int entryID){
        this.token = token;
        this.userID = userID;
        this.queryRequest = new QueryRequest(queryType, entryID);
    }
    /**
     * This is for:
     * Selections, everything
     *
     * @param token The users token
     * @param queryType What type of query to perform
     * @param userID The users ID
     */
    public SQLQueryJson(String token, String queryType, int userID){
        this.token = token;
        this.userID = userID;
        this.queryRequest = new QueryRequest(queryType);
    }

    // Getters
    public int getUserID(){ return this.userID; }
    public ArrayList<DataJsonCalendarEntries> getQueryResponseArrayList(){ return this.queryResponse.getCalendarEntriesArrayList(); }


    // Inner-Classes
    private class QueryRequest{
        // This class is for queries built in android.
        // When this class is being build with the new keyword, i know its for when i want to make a query

        // Fields
        private String queryType;
        private int entryID;
        private DataJsonCalendarEntries dataJsonCalendarEntries;

        // Constructs
        // This is for insertion queries
        private QueryRequest(DataJsonCalendarEntries dataJsonCalendarEntries, String queryType){
            this.dataJsonCalendarEntries = dataJsonCalendarEntries;
            this.queryType = queryType;
        }
        // This is for an update query
        private QueryRequest(DataJsonCalendarEntries dataJsonCalendarEntries, String queryType, int entryID){
            this.dataJsonCalendarEntries = dataJsonCalendarEntries;
            this.queryType = queryType;
            this.entryID = entryID;
        }
        // This is for selecting one post, or a delete query
        private QueryRequest(String queryType, int entryID){
            this.queryType = queryType;
            this.entryID = entryID;
        }
        // This is for selecting all posts
        private QueryRequest(String queryType){
            this.queryType = queryType;
        }
    }
    private class QueryResponse{
        // This class is for mapping, so i can map a json to this .java file
        // It also contains getters, for pulling the data out into my android code

        // Fields
        private ArrayList<DataJsonCalendarEntries> calendarEntriesArrayList = new ArrayList<>();

        // Getters
        public ArrayList<DataJsonCalendarEntries> getCalendarEntriesArrayList() { return this.calendarEntriesArrayList; }
    }
}
