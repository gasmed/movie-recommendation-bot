package com.example.moviebot;

import com.example.moviebot.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MovieBotApplication {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        context.registerShutdownHook();
    }
}