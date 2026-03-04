package com.ecom.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ecom.util.DBConnection;

public class OrderService {

	public static void viewOrderHistory() {

	    Connection con = DBConnection.getConnection();
	    
	    String query ="SELECT o.order_id, o.total_amount, oi.prod_id, p.prod_name, oi.order_qty, oi.price " +
	            "FROM orders o " +
	            "JOIN order_items oi ON o.order_id = oi.order_id " +
	            "JOIN products p ON oi.prod_id = p.prod_id " +
	            "WHERE o.cust_id = 1 " +
	            "ORDER BY o.order_id";

	    try {

	        PreparedStatement ps = con.prepareStatement(query);
	        ResultSet rs = ps.executeQuery();

	        String previousOrderId = "";

	        while (rs.next()) {

	            String currentOrderId = rs.getString("order_id");

	            if (!currentOrderId.equals(previousOrderId)) {

	                System.out.println("\nOrder ID: " + currentOrderId +
	                        "   Total Amount: " + rs.getFloat("total_amount"));

	                System.out.println("--------------------------------------------------");
	                System.out.printf("%-12s %-20s %-10s %-10s%n",
	                        "Product ID", "Product Name", "Qty", "Price");

	                previousOrderId = currentOrderId;
	            }

	            System.out.printf("%-12s %-20s %-10d %-10.2f%n",
	                    rs.getString("prod_id"),
	                    rs.getString("prod_name"),
	                    rs.getInt("order_qty"),
	                    rs.getFloat("price"));
	        }

	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
	}
}
