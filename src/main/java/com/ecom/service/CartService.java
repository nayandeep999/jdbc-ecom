package com.ecom.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ecom.customexception.InvalidProductIdException;
import com.ecom.customexception.UnavailableStocksException;
import com.ecom.util.DBConnection;

public class CartService {

	private static final Logger logger = LogManager.getLogger(CartService.class);

	public void getProductIdByName(String prodName) {

		Connection con = DBConnection.getConnection();
		String query = "select prod_id, prod_name,prod_qty,prod_price from products where prod_name LIKE ?";

		try {
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, prodName + "%");

			logger.info("Searching products starting with name: {}", prodName);

			ResultSet rs = ps.executeQuery();
			boolean found = false;

			System.out.printf("%-12s %-20s %-15s %-15s%n", "Product ID", "Product Name", "Product Qty",
					"Product Price");

			while (rs.next()) {

				found = true;

				String id = rs.getString("prod_id");
				String name = rs.getString("prod_name");
				int qty = rs.getInt("prod_qty");
				float price = rs.getFloat("prod_price");

				System.out.printf("%-12s %-20s %-15d %-15.2f%n", id, name, qty, price);
			}

			if (!found) {
				logger.info("No products found with name starting with: {}", prodName);
			}

		} catch (SQLException e) {
			logger.error("Error while fetching products", e);
		}
	}

	public static boolean checkIfProdExists(String prodId, Connection con) {

		String query = "Select * from products where prod_id= ?";

		try {
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, prodId);

			ResultSet rs = ps.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			logger.error("Error checking product existence for ID {}", prodId, e);
			return false;
		}
	}

	public boolean checkProdQty(String prodId, int qty, Connection con) {

		if (qty <= 0) {
			logger.warn("Invalid quantity requested: {}", qty);
			return false;
		}

		String query = "select * from products where prod_id= ?";

		try {

			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, prodId);

			ResultSet rs = ps.executeQuery();

			int actQty = 0;

			if (rs.next()) {
				actQty = rs.getInt("prod_qty");
			}

			if (actQty >= qty) {
				return true;
			} else {
				throw new UnavailableStocksException("Not enough quantity available");
			}

		} catch (UnavailableStocksException | SQLException e) {
			logger.error("Stock validation failed for product {}", prodId, e);
			return false;
		}
	}

	public static void updateProdQty(String prodId, int qty, Connection con) {

		String query = "update products set prod_qty= prod_qty-? where prod_id=?";

		try {

			PreparedStatement ps = con.prepareStatement(query);
			ps.setInt(1, qty);
			ps.setString(2, prodId);

			int res = ps.executeUpdate();

			if (res > 0) {
				logger.info("Product stock updated successfully for product {}", prodId);
			} else {
				logger.warn("Product stock not updated for product {}", prodId);
			}

		} catch (SQLException e) {
			logger.error("Error updating product quantity for {}", prodId, e);
		}
	}

	public void addToCart(String prodId, int qty) {

		Connection con = DBConnection.getConnection();

		try {

			if (ifProductExistInCartWithQty(prodId, qty, con)) {

				String query = "Update cart set qty=qty+? where prod_id=?";
				PreparedStatement ps = null;

				try {

					con.setAutoCommit(false);

					ps = con.prepareStatement(query);
					ps.setInt(1, qty);
					ps.setString(2, prodId);

					ps.executeUpdate();

					logger.info("Updated quantity in cart for product {}", prodId);

					con.commit();

				} catch (SQLException e) {

					con.rollback();
					logger.error("Transaction failed while updating cart", e);

				} finally {

					if (ps != null) {
						ps.close();
					}
				}

			}

			else if (checkIfProdExists(prodId, con) && checkProdQty(prodId, qty, con)) {

				String query = "Insert into cart(prod_id,cust_id,qty) values(?,?,?)";
				PreparedStatement ps = null;

				try {

					ps = con.prepareStatement(query);
					ps.setString(1, prodId);
					ps.setInt(2, 1);
					ps.setInt(3, qty);

					int count = ps.executeUpdate();

					if (count > 0) {
						logger.info("Product {} added to cart", prodId);
					}

				} catch (SQLException e) {
					logger.error("Error adding product {} to cart", prodId, e);

				} finally {

					if (ps != null)
						ps.close();
					if (con != null)
						con.close();
				}
			}

		} catch (Exception e) {
			logger.error("Unexpected error while adding product to cart", e);
		}
	}

	public boolean ifProductExistInCart(String prodId, Connection con) {

		String query = "Select * from cart where prod_Id = ?";

		try {

			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, prodId);

			ResultSet rs = ps.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			logger.error("Error checking cart for product {}", prodId, e);
		}

		return false;
	}

	public boolean ifProductExistInCartWithQty(String prodId, int qty, Connection con) {

		String cartQuery = "Select * from cart where prod_Id = ?";
		String prodQuery = "Select * from products where prod_id= ?";

		try {

			if (qty <= 0) {
				throw new UnavailableStocksException("Quantity can't be zero or negative");
			}

			if (checkIfProdExists(prodId, con)) {

				PreparedStatement ps = con.prepareStatement(cartQuery);
				ps.setString(1, prodId);

				ResultSet cartRes = ps.executeQuery();

				ps = con.prepareStatement(prodQuery);
				ps.setString(1, prodId);

				ResultSet prodRes = ps.executeQuery();

				int actProdQty = 0;

				if (cartRes.next()) {

					if (prodRes.next()) {
						actProdQty = prodRes.getInt("prod_qty");
					}

					if (actProdQty >= qty) {
						return true;
					} else {
						throw new UnavailableStocksException("Requested quantity not available");
					}
				}

			} else {
				throw new InvalidProductIdException("Invalid product id");
			}

		} catch (InvalidProductIdException | UnavailableStocksException | SQLException e) {
			logger.error("Cart validation failed for product {}", prodId, e);
		}

		return false;
	}

	public void removefromCart(String prodId) {

		Connection con = DBConnection.getConnection();
		PreparedStatement ps = null;

		// check if product exists in cart
		if (!ifProductExistInCart(prodId, con)) {
			logger.warn("Attempted to remove product {} which does not exist in cart", prodId);
			return;
		}

		String fetchQty = "select qty from cart where prod_Id= ?";
		int qty = 0;

		try {
			ps = con.prepareStatement(fetchQty);
			ps.setString(1, prodId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				qty = rs.getInt("qty");
			}

		} catch (SQLException e) {
			logger.error("Error fetching cart quantity for product {}", prodId, e);
		}

		String delQuery = "Delete from cart where prod_Id=?";

		try {

			ps = con.prepareStatement(delQuery);
			ps.setString(1, prodId);

			int res = ps.executeUpdate();

			if (res > 0) {
				logger.info("Product {} removed from cart", prodId);

				// updateProdQty(prodId, -qty, con);

			} else {
				logger.warn("Product {} could not be removed from cart", prodId);
			}

		} catch (SQLException e) {
			logger.error("Error removing product {} from cart", prodId, e);
		}
	}

	public static void viewCartItems() {

		Connection con = DBConnection.getConnection();

		String query = "SELECT c.prod_id, p.prod_name, c.qty, p.prod_price, (c.qty*p.prod_price) as total_item_price "
				+ "FROM cart c JOIN products p ON c.prod_id = p.prod_id WHERE c.cust_id = 1";

		try {

			PreparedStatement ps = con.prepareStatement(query);
			ResultSet rs = ps.executeQuery();

			System.out.printf("%-12s %-20s %-10s %-10s %-15s%n", "Product ID", "Product Name", "Qty", "Price",
					"Total Item Price");

			float billingAmount = 0;

			while (rs.next()) {

				float price = rs.getFloat("prod_price");
				float totalItemPrice = rs.getFloat("total_item_price");

				System.out.printf("%-12s %-20s %-10d %-10.2f %-10.2f%n", rs.getString("prod_id"),
						rs.getString("prod_name"), rs.getInt("qty"), price, totalItemPrice);

				billingAmount += totalItemPrice;
			}

			logger.info("Total Billing Amount: {}", billingAmount);

		} catch (Exception e) {
			logger.error("Error fetching cart items", e);
		}
	}
}