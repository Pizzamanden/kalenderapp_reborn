package com.example.kalenderapp_reborn.supportclasses;

import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpRequestBuilder {

    private String url;

    public HttpRequestBuilder(String siteURL){
        url = siteURL;
    }


    // Singular get
    public Request getBuilder(String requestName, String requestValue){
        // Build url string
        String concatUrl = getUrl() + "?" + requestName + "=" + requestValue;
        // Build request with url string
        Request request = new Request.Builder()
                .url(concatUrl)
                .build();
        // Return Request
        return request;
    }

    // Singular post
    public Request postBuilder(String requestName, String requestValue){
        // Build request body
        // Only needs requestname and value
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(requestName, requestValue)
                .build();
        // Build request
        Request request = new Request.Builder()
                .url(getUrl())
                .post(requestBody)
                .build();
        // Return request
        return request;
    }

    // Arraylist get
    public Request getBuilder(ArrayList<String> requestName, ArrayList<String> requestValue){
        // Build url string
        String concatUrl = getUrl() + "?";
        for(int i = 0; i < requestName.size(); i++){
            // Check if its the first part of the arraylist
            // If not, add a "&" to add more parameters
            if(i == 0){
                concatUrl += requestName.get(i) + "=" + requestValue.get(i);
            } else {
                concatUrl += "&" + requestName.get(i) + "=" + requestValue.get(i);
            }
        }
        // Build request with url string
        Request request = new Request.Builder()
                .url(concatUrl)
                .build();
        // Return Request
        return request;
    }

    // Arraylist post
    public Request postBuilder(ArrayList<String> requestName, ArrayList<String> requestValue){
        // Start building multipart body
        MultipartBody.Builder mBody = new MultipartBody.Builder();
        mBody.setType(MultipartBody.FORM);
        for(int i = 0; i < requestName.size(); i++){
            mBody.addFormDataPart(requestName.get(i), requestValue.get(i));
        }
        // Build Body
        RequestBody requestBody = mBody.build();
        // Build request with body and url
        Request request = new Request.Builder()
                .url(getUrl())
                .post(requestBody)
                .build();
        // Return Request
        return request;
    }

    private String getUrl(){
        return this.url;
    }
}
