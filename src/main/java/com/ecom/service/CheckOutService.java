package com.ecom.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ecom.util.DBConnection;

public class CheckOutService {
	public static void checkOut() {
		Connection con = DBConnection.getConnection();

		String createOrderQuery = "Insert into orders(cust_id,total_amount) values (?,?) ";
		
		
		String fetchFromCart="SELECT c.cart_id, c.cust_id, c.prod_id, c.qty, p.prod_price "
				+ "FROM cart c JOIN products p ON c.prod_id = p.prod_id WHERE c.cust_id = 1;";
		
		String fetchOrderId = "select order_id from orders where cust_id=?";
		String insertIntoOrderItems=" Insert into order_items(order_id,prod_id,price,order_qty) values (?,?,?,?)";
		String totalAmountQuery = "SELECT SUM(CAST(price * order_qty AS DECIMAL(18,2))) AS total_amount from order_items";
		String updateOrders = "update orders set total_amount = ? where order_id=?";
		String fetchFromOrderItems = "select prod_id, order_qty from order_items where order_id = ?";
		String updateProductStocks = "update products set prod_qty= prod_qty -? where prod_id=?";
		String clearCart = "delete from cart where cust_id = 1";
		PreparedStatement ps = null;

		try {
			
			ps = con.prepareStatement(createOrderQuery);
			ps.setInt(1, 1);
			ps.setInt(2, 0);
			
			con.setAutoCommit(false);
			
			ps.execute();
			
			ps = con.prepareStatement(fetchFromCart);
			ResultSet resultFromCart = ps.executeQuery();
			
			ps = con.prepareStatement(fetchOrderId);
			ps.setInt(1, 1);
			ResultSet orderIdFromOrders = ps.executeQuery();
			
			String orderId = null;
			
			while(orderIdFromOrders.next()) {
				orderId = orderIdFromOrders.getString("order_id");
			}
			
			ps = con.prepareStatement(insertIntoOrderItems);
			
			while(resultFromCart.next()) {
				ps.setString(1, orderId);
				ps.setString(2, resultFromCart.getString("prod_id"));
				ps.setString(3, resultFromCart.getString("prod_price"));
				ps.setString(4, resultFromCart.getString("qty"));
				ps.addBatch();
			}
			
			ps.executeBatch();
			
			ps = con.prepareStatement(totalAmountQuery);
			ResultSet totalAmountResult = ps.executeQuery();
			float totalAmount = 0;
			
			while(totalAmountResult.next()) {
				totalAmount = totalAmountResult.getFloat("total_amount");
			}
			
			ps = con.prepareStatement(updateOrders);
			ps.setFloat(1, totalAmount);
			ps.setString(2, orderId);
			
			ps.execute();
			
			ps = con.prepareStatement(fetchFromOrderItems);
			ps.setString(1, orderId);
			ResultSet qtyFromOrderItems = ps.executeQuery();
			
			ps = con.prepareStatement(updateProductStocks);
			
			while(qtyFromOrderItems.next()) {
				ps.setString(2, qtyFromOrderItems.getString("prod_id"));
				ps.setInt(1, qtyFromOrderItems.getInt("order_qty"));
				ps.addBatch();
			}
			
			ps.executeBatch();
			
			ps = con.prepareStatement(clearCart);
			ps.execute();
			
			con.commit();
			
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				System.out.println(e.getMessage());
			}
			
			System.out.println(e.getMessage());
		} finally {
			try {
				ps.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void main(String[] args) {
		CheckOutService.checkOut();
	}
} 
