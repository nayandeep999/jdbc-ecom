package com.ecom.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ecom.util.DBConnection;

public class ViewProducts {

	public void showAllProducts() {
		Connection con = DBConnection.getConnection();
		String query = "Select * from products order by prod_name ";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(query);

			ResultSet rs = ps.executeQuery();

			System.out.printf("%-12s %-20s %-15s %-15s%n", "Product ID", "Product Name", "Product Qty",
					"Product Price");
			System.out.println("---------------------------------------------------------------------");

			while (rs.next()) {
				String id = rs.getString(1);
				String name = rs.getString(2);
				int qty = rs.getInt(3);
				float price = rs.getFloat(4);

				System.out.printf("%-12s %-20s %-15d %-15.2f%n", id, name, qty, price);

			}
		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				// will change later
				System.out.println(e.getMessage());
			}
		}
	}
	
}
