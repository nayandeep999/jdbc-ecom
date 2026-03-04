package com.ecom.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ecom.util.DBConnection;

public class CreateUser 
{
	
	public void createSampleUser() throws SQLException
	{
		Connection con = DBConnection.getConnection();
		String query="Insert into customers(cust_name,email) values(?,?)";
		
		PreparedStatement ps = con.prepareStatement(query);
		
		ps.setString(1, "user123");
		ps.setString(2, "user123@gmail.com");
			
		ps.executeUpdate();
	}
	

}
