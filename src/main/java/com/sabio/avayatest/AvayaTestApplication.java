package com.sabio.avayatest;

import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sabio.avayatest.AvayaChat;

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
        
        System.out.println("Enter your message: ");
        Scanner scanner = new Scanner(System.in);
        String message = scanner.nextLine();
        if(!message.equalsIgnoreCase("")) {
             
             try {
            	 chat.chatInitiation();
        	 	 chat.sendMessage(message);
             } catch (Exception e) {
            	 System.out.println("Something went wrong");
             }
        	 
        }
        scanner.close();
        
        System.exit(0);
        
    }
    
}
