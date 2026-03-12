package com.ecom.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBConnection {

	private static final Logger logger = LogManager.getLogger(DBConnection.class);

	private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "0000";

	public static Connection getConnection() {

		Connection connection = null;

		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

			logger.info("Database connection established");

		} catch (ClassNotFoundException e) {

			logger.error("MySQL JDBC Driver not found", e);

		} catch (SQLException e) {

			logger.error("Database connection failed", e);
		}

		return connection;
	}
}