package com.ecom.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ecom.util.DBConnection;

public class OrderService {

	private static final Logger logger = LogManager.getLogger(OrderService.class);

	public static void viewOrderHistory() {

		Connection con = DBConnection.getConnection();

		String query = "SELECT o.order_id, o.total_amount, oi.prod_id, p.prod_name, oi.order_qty, oi.price "
				+ "FROM orders o " + "JOIN order_items oi ON o.order_id = oi.order_id "
				+ "JOIN products p ON oi.prod_id = p.prod_id " + "WHERE o.cust_id = 1 " + "ORDER BY o.order_id";

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			logger.info("Fetching order history for customer {}", 1);

			ps = con.prepareStatement(query);
			rs = ps.executeQuery();

			String previousOrderId = "";

			while (rs.next()) {

				String currentOrderId = rs.getString("order_id");

				if (!currentOrderId.equals(previousOrderId)) {

					logger.info("Displaying order {}", currentOrderId);

					System.out.println(
							"\nOrder ID: " + currentOrderId + "   Total Amount: " + rs.getFloat("total_amount"));

					System.out.println("--------------------------------------------------");
					System.out.printf("%-12s %-20s %-10s %-10s%n", "Product ID", "Product Name", "Qty", "Price");

					previousOrderId = currentOrderId;
				}

				System.out.printf("%-12s %-20s %-10d %-10.2f%n", rs.getString("prod_id"), rs.getString("prod_name"),
						rs.getInt("order_qty"), rs.getFloat("price"));
			}

		} catch (SQLException e) {

			logger.error("Error fetching order history", e);

		} finally {

			try {

				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (con != null)
					con.close();

			} catch (SQLException e) {

				logger.error("Error closing database resources", e);
			}
		}
	}
}