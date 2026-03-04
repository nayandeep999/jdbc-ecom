package com.ecom.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ecom.customexception.InvalidProductIdException;
import com.ecom.customexception.UnavailableStocksException;
import com.ecom.util.DBConnection;

public class CartService {
	
	public void getProductIdByName(String prodName) {

	    Connection con = DBConnection.getConnection();

	    String query = "select prod_id, prod_name,prod_qty,prod_price from products where prod_name LIKE ?";

	    try {
	        PreparedStatement ps = con.prepareStatement(query);
//	        ps.setString(1, prodName);
	        
	        
	       // ps.setString(1, "%" + prodName + "%");
	        
	        ps.setString(1, prodName +"%");

	        ResultSet rs = ps.executeQuery();
	        boolean found= false;
	        
	        System.out.printf("%-12s %-20s %-15s %-15s%n", "Product ID", "Product Name", "Product Qty",
					"Product Price");
			System.out.println("---------------------------------------------------------------------");
			
			
	        while (rs.next()) {
	        		found=true;
	        	
	        		
	        		String id= rs.getString("prod_id");
	        		String name=rs.getString("prod_name");
	        		int qty= rs.getInt("prod_qty");
	        		float price= rs.getFloat("prod_price");
	        		
	        		System.out.printf("%-12s %-20s %-15d %-15.2f%n", id, name, qty, price);
	        }
	        
	        if(!found) {
	        	System.out.println("No products found with that name.");
	        }

	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
	}
	
	

	// this method will check if product Exist in our database(products table) or not..
	public static boolean checkIfProdExists(String prodId, Connection con) {
		String query = "Select * from products  where prod_id= ?";

		PreparedStatement ps;
		try {
			ps = con.prepareStatement(query);
			ps.setString(1, prodId);
			ResultSet rs = ps.executeQuery();
			if(!rs.next())
			{
				return false;
			}
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}

	}

	// This method will check the stock availability of our ProductItem.
	public boolean checkProdQty(String prodId, int qty, Connection con) {
		
		if(qty<=0) {
			return false;
		}
		
		String query = "select * from products where prod_id= ?";

		PreparedStatement ps;

		try {
			ps = con.prepareStatement(query);
			ps.setString(1, prodId);
			ResultSet rs = ps.executeQuery();
			int actQty = 0;
			if (rs.next()) {
				actQty = rs.getInt("prod_qty");
			}

		
			if (actQty >= qty) {

				return true;
			} else {

				throw new UnavailableStocksException("Not enough quantity.."); // throwing custom exception..
			}

		} catch (UnavailableStocksException | SQLException e) {
			System.out.println(e.getMessage());
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
				System.out.println("Product Stock updated successfully..");
			} else {
				System.out.println("Product stock not updated...");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// the below methods add the items in a cart.
	public void addToCart(String prodId, int qty) {
		Connection con = DBConnection.getConnection();
		
		try {
			
			if (ifProductExistInCartWithQty(prodId, qty, con)) {
				String query = "Update cart set  qty=qty+? where prod_id=?";
				PreparedStatement ps = null;

				try {
					con.setAutoCommit(false);
					ps = con.prepareStatement(query);                                
					ps.setInt(1, qty);
					ps.setString(2, prodId);

					ps.executeUpdate();
					System.out.println("Product Id: " + prodId + " quantity updated in cart.");

					con.commit();
				} catch (SQLException e) {
					try {
						con.rollback();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} finally {
					try {
						ps.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						// updateProdQty(prodId,qty,con);
						System.out.println(count + " Product added to cart..");
					}
				}

				catch (SQLException e) {
					System.out.println("Not enough stock avaialable...");
					e.printStackTrace();
				} finally {
					try {
						ps.close();
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		
	}

	// method for checking existence of Product in Cart
	public boolean ifProductExistInCart(String prodId, Connection con) {
		String query = "Select * from cart where prod_Id = ?";

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(query);

			ps.setString(1, prodId);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return true;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());

		}
		return false;
	}

	public boolean ifProductExistInCartWithQty(String prodId, int qty, Connection con) {
		String cartQuery = "Select * from cart where prod_Id = ?";

		String prodQuery = "Select * from products where prod_id= ?";

		PreparedStatement ps = null;
		try {

			ps = con.prepareStatement(cartQuery);
			
			if(qty <=0) {
				throw new UnavailableStocksException("Quantity can't be zero or negative");
			}

			if (checkIfProdExists(prodId, con)) {

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
						throw new UnavailableStocksException("Requested quantities not available in stock..");
					}
				}

			} else {
				throw new InvalidProductIdException("Invalid product id");
			}

		} catch (InvalidProductIdException | UnavailableStocksException | SQLException e) {
			System.out.println(e.getMessage());

		}

		return false;

	}

	// this method will help to remove product and simultaneously update product
	// stock back accordingly..
	public void removefromCart(String prodId) {
		Connection con = DBConnection.getConnection();
		PreparedStatement ps = null;
		// if product not exist in cart
		if (!ifProductExistInCart(prodId, con)) {
			System.out.println("Trying to remove product which does not even exist in cart..");
			return;
		}

		else {

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String delQuery = "Delete from cart where prod_Id=?";
			try {
				ps = con.prepareStatement(delQuery);

				ps.setString(1, prodId);

				int res = ps.executeUpdate();

				if (res > 0) {
					// updateProdQty(prodId, -qty, con);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	// Fetch cart items from DB
	public static void viewCartItems() {

		Connection con = DBConnection.getConnection();

		String query = "SELECT c.prod_id, p.prod_name, c.qty, p.prod_price, (c.qty*p.prod_price) as total_item_price "
				+ "FROM cart c JOIN products p ON c.prod_id = p.prod_id WHERE c.cust_id = 1";

		try {

			PreparedStatement ps = con.prepareStatement(query);
			ResultSet rs = ps.executeQuery();

			System.out.printf("%-12s %-20s %-10s %-10s %-15s%n", "Product ID", "Product Name", "Qty", "Price",
					"Total Item Price");

			System.out.println("----------------------------------------------------------------------------------");

			float billingAmount = 0;

			while (rs.next()) {

				float price = rs.getFloat("prod_price");
				float totalItemPrice = rs.getFloat("total_item_price");

				System.out.printf("%-12s %-20s %-10d %-10.2f %-10.2f%n", rs.getString("prod_id"),
						rs.getString("prod_name"), rs.getInt("qty"), price, totalItemPrice);

				billingAmount += totalItemPrice;
			}

			System.out.println("---------------------------------------------------------------------------");
			System.out.println("Total Billing Amount: " + billingAmount);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	
}
