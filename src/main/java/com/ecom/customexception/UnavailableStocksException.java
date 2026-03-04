package com.ecom.customexception;

@SuppressWarnings("serial")
public class UnavailableStocksException extends Exception
{
	public UnavailableStocksException(String msg) 
	{
		super(msg);
	}
}
