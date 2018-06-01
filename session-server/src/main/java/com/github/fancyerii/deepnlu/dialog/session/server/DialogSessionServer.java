package com.github.fancyerii.deepnlu.dialog.session.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;  

@SpringBootApplication(scanBasePackages = "com.github.fancyerii.deepnlu")
public class DialogSessionServer {

    public static void main(String[] args) {
        SpringApplication.run(DialogSessionServer.class, args);
    }

}
