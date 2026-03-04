package com.ecom.ui;

import java.util.Scanner;

import com.ecom.service.CartService;
import com.ecom.service.CheckOutService;
import com.ecom.service.OrderService;
import com.ecom.service.ViewProducts;

public class ConsoleHandler {

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
                viewProducts.showAllProducts();
                break;

            case 2:
                CartService cs = new CartService();

            		System.out.print("Enter Product Name: ");
                String prodName = sc.next();
                
                cs.getProductIdByName(prodName);
                
                System.out.print("\nEnter Product ID: ");
                String prodId = sc.next();
                

                System.out.print("Enter Quantity: ");
                int qty = sc.nextInt();

                cs.addToCart(prodId, qty);

                break;

            case 3:
                System.out.print("Enter Product ID to remove: ");
                String removeId = sc.next();

                cartService.removefromCart(removeId);
              //  System.out.println("Product removed from cart.");
                break;

            case 4:
                CartService.viewCartItems();
                break;

            case 5:
                CheckOutService.checkOut();
                System.out.println("Order placed successfully.");
                break;
             
            case 6:
                OrderService.viewOrderHistory();
                break;
            case 7:
                System.out.println("Thank you for shopping with us!!!");
                System.exit(0);

            default:
                System.out.println("Invalid choice.");
            }
        }
    }


}
