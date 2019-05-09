package com.sabio.avayatest;

import java.util.Scanner;

public class MessageSender extends Thread {

	private Scanner _scanner = null;
	private AvayaChat _chat = null;

	public MessageSender(Scanner scanner, AvayaChat chat) {
		_scanner = scanner;
		_chat = chat;
	}

	@Override
	public void run() {

		System.out.println("Enter your message: ");
		String inputLine = _scanner.nextLine();
		if (!inputLine.equalsIgnoreCase("")) {
			try {
				_chat.sendMessage(inputLine);
			} catch (Exception e) {
				System.out.println("Something went wrong");
			}
		}
	}

}
