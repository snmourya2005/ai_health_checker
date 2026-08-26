package com.aihealth.healthchecker.exception;

public class userAlreadyExistException extends RuntimeException{
    public userAlreadyExistException(String message){
        super(message);
    }
}
