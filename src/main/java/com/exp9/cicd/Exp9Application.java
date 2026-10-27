package com.exp9.cicd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Experiment 9 - CI/CD pipeline demo application.
 * A tiny "Product Catalogue" REST service that we build, test, containerize
 * and release (canary) through GitHub Actions.
 */
@SpringBootApplication
public class Exp9Application {

    public static void main(String[] args) {
        SpringApplication.run(Exp9Application.class, args);
    }
}
