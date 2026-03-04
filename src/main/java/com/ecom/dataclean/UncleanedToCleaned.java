package com.ecom.dataclean;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.opencsv.CSVReader;

public class UncleanedToCleaned {
	private static final String url = "jdbc:mysql://localhost:3306/ecommerce_db";
	private static final String userName = "root";
	private static final String pswrd = "0000";

	private static final int BATCH_SIZE = 50;

	// Using hashset to maintain unq prod_id and name.
	private static Set<String> unqProdId = new HashSet<>();
	private static Set<String> unqProdName = new HashSet<>();

	private static boolean validateData(String prodId, String prodName, String prodQty, String prodPrice) {
		boolean flag = true;

		// VALLIDATING THE PROD ID
		if (prodId == null || unqProdId.contains(prodId) || prodId.trim().isEmpty()) {

			return false;
		} else {
			unqProdId.add(prodId);
		}

		// VALIDATE THE NAME
		if (prodName == null || unqProdName.contains(prodName) || !prodName.matches("^[A-Za-z][A-Za-z0-9 _-]{1,49}$")
				|| prodName.trim().isEmpty()) {

			return false;
		} else {
			unqProdName.add(prodName);
		}

		// VALIDATE PRODUCT PRICE
		if (prodPrice == null || prodPrice.trim().isEmpty() || !prodPrice.matches("\\d+")) {

			flag = false;
		} else {
			int price = Integer.parseInt(prodPrice);
			if (price <= 0) {

				flag = false;
			}
		}

		// VALIDATING THE PRODUCT QUANTITY
		if (prodQty == null || prodQty.trim().isEmpty() || !prodQty.matches("\\d+")) {
			flag = false;

		} else {
			int productQty = Integer.parseInt(prodQty);
			if (productQty <= 0) {

				flag = false;
			}
		}

		return flag;
	}

	public static void main(String[] args) {

		Connection connect = null;
		PreparedStatement ps = null;

		try {
			connect = DriverManager.getConnection(url, userName, pswrd);

			String validProd = "Insert into products values(?,?,?,?)";

			ps = connect.prepareStatement(validProd);

			// this line of code is reading csv file..
			CSVReader read = new CSVReader(new FileReader("E:\\products.csv"));

			connect.setAutoCommit(false);

			int count = 0;
			String prodDet[];

			read.readNext(); // skipping header

			// this loop will each row from csv file until EOF.

			while ((prodDet = read.readNext()) != null) {

				String prodId = "PR" + prodDet[0];
				String prodName = prodDet[1];
				String prodPrice = prodDet[2];
				String prodQty = prodDet[3];

				if (validateData(prodId, prodName, prodQty, prodPrice)) {

					ps.setString(1, prodId);
					ps.setString(2, prodName);
					ps.setInt(3, Integer.parseInt(prodQty));
					ps.setFloat(4, Float.parseFloat(prodPrice));

					ps.addBatch();
					count++;

					// Inserting data in a batch
					if (count % BATCH_SIZE == 0)
						ps.executeBatch();
				}
			}

			// Inserting remaining batch
			ps.executeBatch();

			// This will save the data permanentaly in DB
			connect.commit();

			System.out.println("Data loaded successfully");

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			try {
				connect.rollback(); // Roll Back if Insertion Failed while loading Data
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

		} finally {
			if (connect != null || ps != null) {
				try {
					connect.close();
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

	}
}