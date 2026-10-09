package com.example.dockerpoc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() throws Exception {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("app", "docker-crud-poc");
        info.put("message", "Employee CRUD is running");
        info.put("hostname (container id in Docker)", InetAddress.getLocalHost().getHostName());
        info.put("employees api", "/api/employees");
        info.put("h2 console", "/h2-console");
        info.put("health", "/actuator/health");
        return info;
    }
}
