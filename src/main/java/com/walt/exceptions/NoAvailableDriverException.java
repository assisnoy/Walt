package com.walt.exceptions;

public class NoAvailableDriverException extends Exception{
   public NoAvailableDriverException (){
        super("There isn't an available driver");
    }
}
