package com.ecom.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "0000";

	public static Connection getConnection() {

		Connection connection = null;

		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

			// System.out.println("Database connected successfully");

		} catch (ClassNotFoundException e) {
			System.out.println("JDBC Driver not found");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("Connection failed");
			e.printStackTrace();
		}

		return connection;
	}
}