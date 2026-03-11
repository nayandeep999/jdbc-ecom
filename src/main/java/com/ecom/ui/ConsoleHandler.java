package com.ecom.ui;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ecom.service.CartService;
import com.ecom.service.CheckOutService;
import com.ecom.service.OrderService;
import com.ecom.service.ViewProducts;

public class ConsoleHandler {

	private static final Logger logger = LogManager.getLogger(ConsoleHandler.class);
	static Scanner sc = new Scanner(System.in);

	public static void showUI() {

		CartService cartService = new CartService();
		ViewProducts viewProducts = new ViewProducts();

		while (true) {

			System.out.println("\n========= E-COMMERCE MENU =========");
			System.out.println("1. View Products");
			System.out.println("2. Add Product To Cart");
			System.out.println("3. Remove Product From Cart");
			System.out.println("4. View Cart Items");
			System.out.println("5. Checkout");
			System.out.println("6. Order History");
			System.out.println("7. Exit");

			System.out.print("Enter choice: ");

			int choice = sc.nextInt();

			switch (choice) {

			case 1:

				logger.info("User selected: View Products");
				viewProducts.showAllProducts();
				break;

			case 2:

				System.out.print("Enter Product Name: ");
				String prodName = sc.next();

				cartService.getProductIdByName(prodName);

				System.out.print("\nEnter Product ID: ");
				String prodId = sc.next();

				System.out.print("Enter Quantity: ");
				int qty = sc.nextInt();

				logger.info("User adding product {} with quantity {}", prodId, qty);

				cartService.addToCart(prodId, qty);

				break;

			case 3:

				System.out.print("Enter Product ID to remove: ");
				String removeId = sc.next();

				logger.info("User attempting to remove product {} from cart", removeId);

				cartService.removefromCart(removeId);

				break;

			case 4:

				logger.info("User viewing cart items");

				CartService.viewCartItems();

				break;

			case 5:

				logger.info("User initiated checkout");

				CheckOutService.checkOut();

				System.out.println("Order placed successfully.");

				break;

			case 6:

				logger.info("User viewing order history");

				OrderService.viewOrderHistory();

				break;

			case 7:

				logger.info("User exited the application");

				System.out.println("Thank you for shopping with us!!!");

				sc.close();
				System.exit(0);

			default:

				logger.warn("Invalid menu choice entered: {}", choice);

				System.out.println("Invalid choice.");
			}
		}
	}
}