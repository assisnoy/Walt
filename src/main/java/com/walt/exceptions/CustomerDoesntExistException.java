package com.walt.exceptions;

public class CustomerDoesntExistException extends Exception{
    public CustomerDoesntExistException(){
        super("Given customer doesn't exist in the system");
    }
}
