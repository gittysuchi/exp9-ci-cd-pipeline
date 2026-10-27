package com.example.exp9;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.boot.WebApplicationType;

@Provider("product-provider")
@PactFolder("src/test/resources/pacts")
class ProductProviderPactTest {
    private static ConfigurableApplicationContext app;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        if (app == null) {
            app = new SpringApplication(Exp9Application.class).run(
                    "--server.port=0", "--spring.main.web-application-type=servlet");
        }
        int port = ((ServletWebServerApplicationContext) app).getWebServer().getPort();
        context.setTarget(new HttpTestTarget("localhost", port, "/"));
    }

    @State("products exist")
    void productsExist() {
        // The sample ProductService provides deterministic in-memory products.
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
