package com.syv.data.Diffyne.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@Profile({"demo-source", "demo-target"})
public class DemoCustomerController {

    private final Environment environment;

    public DemoCustomerController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/customers")
    public List<Map<String, Object>> customers() {
        if (environment.acceptsProfiles(Profiles.of("demo-target"))) {
            return targetCustomers();
        }

        return sourceCustomers();
    }

    private static List<Map<String, Object>> sourceCustomers() {
        return List.of(
                customer("C-001", "Alice Jones", "active", 100.00),
                customer("C-002", "Bob Smith", "active", 200.00),
                customer("C-003", "Carol White", "inactive", 300.00),
                customer("C-004", "Source Only", "pending", 400.00)
        );
    }

    private static List<Map<String, Object>> targetCustomers() {
        return List.of(
                customer("C-001", "Alice Jones", "active", 100.00),
                customer("C-002", "Bob Smith", "suspended", 200.00),
                customer("C-003", "Carol White", "inactive", 300.05),
                customer("C-005", "Target Only", "active", 500.00)
        );
    }

    private static Map<String, Object> customer(String id, String name, String status, double balance) {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("id", id);
        customer.put("name", name);
        customer.put("status", status);
        customer.put("balance", balance);
        return customer;
    }
}
