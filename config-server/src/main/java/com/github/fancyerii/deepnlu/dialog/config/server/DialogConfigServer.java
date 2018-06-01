package com.github.fancyerii.deepnlu.dialog.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;  

@SpringBootApplication(scanBasePackages = "com.github.fancyerii.deepnlu")
public class DialogConfigServer {

    public static void main(String[] args) {
        SpringApplication.run(DialogConfigServer.class, args);
    }

}
