package com.sabio.avayatest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AvayaTestApplication implements CommandLineRunner {

	@Autowired
	private AvayaChat chat;

	private static Logger LOG = LoggerFactory.getLogger(AvayaTestApplication.class);

	public static void main(String[] args) {
		LOG.info("STARTING THE APPLICATION");

		// disabled banner, don’t want to see the spring logo
		SpringApplication app = new SpringApplication(AvayaTestApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);

		LOG.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) throws IOException {
		LOG.info("EXECUTING : command line runner");

		for (int i = 0; i < args.length; ++i) {
			LOG.info("args[{}]: {}", i, args[i]);
		}
		Scanner scanner = new Scanner(System.in);

		System.out.println("Welcome to the Avaya test chat Application");
		System.out.println("Do you want to initialise your session (y/n)");
		String inputLine = scanner.nextLine();
		if (inputLine.equalsIgnoreCase("y")) {
			try {
				chat.chatInitiation();
				System.out.println("Succesful Initialisation");
				System.out.println("Waiting for a response from Avaya");
				boolean responseFromAvaya = false;

				do {
					ArrayList<String> currentMessages = chat.pollEvents();
					// print any messages we have
					currentMessages.forEach((s) -> {
						if (!s.equalsIgnoreCase("chatAvailable") && !s.equalsIgnoreCase("disconnected")) {
							System.out.println(s);
						}
					});

					if (currentMessages.contains("chatAvailable")) {
						responseFromAvaya = true;

						// we can now send a messages.
						// Running in separate thread to save blocking
						MessageSender sender = new MessageSender(scanner, chat);
						sender.run();

					} else if (chat.pollEvents().contains("disconnected")) {
						System.out.println("Sorry you've been disconected. Closing down");
						scanner.close();
						System.exit(0);
					}
					Thread.sleep(2000);
				} while (responseFromAvaya);
			} catch (Exception e) {
				System.out.println("Sorry initialisation failed. Closing down");
				System.out.println(e.getMessage());
			}

		} else {
			System.out.println("OK I'll close down now");
			scanner.close();
			System.exit(0);
		}

		scanner.close();
		System.exit(0);

	}

}
