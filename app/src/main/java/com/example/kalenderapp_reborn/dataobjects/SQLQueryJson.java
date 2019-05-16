package com.example.kalenderapp_reborn.dataobjects;

import com.example.kalenderapp_reborn.supportclasses.SessionManager;

import java.util.ArrayList;



/*

    Welcome to my SQL Gson file
    In here, i attempt to make a class that takes a construct when build, or contains getters when being mapped to
    Hopefully, this class can handle all my SQL queries that i make

    What i want to send to the server:
    -The users token
    -A query type
    -Maybe a table row (insert)
    -Maybe an ID' for a table row (select and/or delete)
    -Maybe both of the above (update)

    What i want back:
    -A Response code and/or a message
    -Maybe a row
    -Maybe more than one row

 */







public class SQLQueryJson {

    // Fields
    private TokenValidation tokenValidation;
    private QueryRequest queryRequest;
    private QueryResponse queryResponse;

    /**
     * This is for:
     * Insertions
     *
     * @param manager The users token
     * @param calendarEntriesTable Object, styled as a row from CalendarEntries DB
     * @param queryType What type of query to perform
     */
    public SQLQueryJson(SessionManager manager, CalendarEntriesTable calendarEntriesTable, String queryType){
        this.tokenValidation = new TokenValidation(manager.getLastValidation(), manager.getToken());
        this.queryRequest = new QueryRequest(calendarEntriesTable, queryType);
    }
    /**
     * This is for:
     * Updates
     *
     * @param manager The users token
     * @param calendarEntriesTable Object, styled as a row from CalendarEntries DB
     * @param queryType What type of query to perform
     * @param entryID The ID of the entry
     */
    public SQLQueryJson(SessionManager manager, CalendarEntriesTable calendarEntriesTable, String queryType, int entryID){
        this.tokenValidation = new TokenValidation(manager.getLastValidation(), manager.getToken());
        this.queryRequest = new QueryRequest(calendarEntriesTable, queryType, entryID);
    }
    /**
     * This is for:
     * Selections, singular
     * Deletions
     *
     * @param manager The users token
     * @param queryType What type of query to perform
     * @param entryID The ID of the entry
     */
    public SQLQueryJson(SessionManager manager, String queryType, int entryID){
        this.tokenValidation = new TokenValidation(manager.getLastValidation(), manager.getToken());
        this.queryRequest = new QueryRequest(queryType, entryID);
    }
    /**
     * This is for:
     * Selections, everything
     *
     * @param manager The users token
     * @param queryType What type of query to perform
     */
    public SQLQueryJson(SessionManager manager, String queryType){
        this.tokenValidation = new TokenValidation(manager.getLastValidation(), manager.getToken());
        this.queryRequest = new QueryRequest(queryType);
    }

    // Getters
    public ArrayList<CalendarEntriesTable> getQueryResponseArrayList(){ return this.queryResponse.getCalendarEntriesArrayList(); }
    public TokenValidation getTokenValidation(){ return this.tokenValidation; }



    // Inner-Classes
    private class QueryRequest{
        // This class is for queries built in android.
        // When this class is being build with the new keyword, i know its for when i want to make a query

        // Fields
        private String queryType;
        private int entryID;
        private CalendarEntriesTable calendarEntriesTable;

        // Constructs
        // This is for insertion queries
        private QueryRequest(CalendarEntriesTable calendarEntriesTable, String queryType){
            this.calendarEntriesTable = calendarEntriesTable;
            this.queryType = queryType;
        }
        // This is for an update query
        private QueryRequest(CalendarEntriesTable calendarEntriesTable, String queryType, int entryID){
            this.calendarEntriesTable = calendarEntriesTable;
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
        // This class is for mapping, so i can map a json to this class
        // It also contains getters, for pulling the data out into my android code

        // Fields
        private ArrayList<CalendarEntriesTable> calendarEntriesArrayList = new ArrayList<>();
        private String responseMessage;

        // Getters

        public ArrayList<CalendarEntriesTable> getCalendarEntriesArrayList() { return this.calendarEntriesArrayList; }
        public String getResponseMessage() { return this.responseMessage; }
    }
}
