package com.ecom.customexception;

public class InvalidProductIdException extends Exception{
	
	public InvalidProductIdException(String msg){
		super(msg);
	}

}
