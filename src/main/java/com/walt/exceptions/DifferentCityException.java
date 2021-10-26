package com.walt.exceptions;

public class DifferentCityException extends Exception{
    public DifferentCityException(){
        super("Given customer doesn't live in the same city of the restaurant");
    }
}
