package com.google.sps.servlets;

public class NotLoggedInException 
  extends RuntimeException {
    public NotLoggedInException(String errorMessage) {
        super(errorMessage);
    }
}