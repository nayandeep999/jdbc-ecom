package com.ecom.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ecom.ui.ConsoleHandler;

public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {

		logger.info("======================================================");
		logger.info("             WELCOME TO E-COMMERCE SYSTEM             ");
		logger.info("======================================================");
		logger.info("Browse products, manage your cart and place orders.");
		System.out.println();

		ConsoleHandler.showUI();

	}

}
