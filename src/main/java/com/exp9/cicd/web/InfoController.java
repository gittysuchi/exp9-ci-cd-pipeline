package com.exp9.cicd.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tells the caller which release track answered the request.
 * During a canary release this lets us SEE which users hit "stable" and which hit "canary".
 */
@RestController
@RequestMapping("/api/info")
public class InfoController {

    private final String version;
    private final String track;

    public InfoController(@Value("${app.version}") String version, @Value("${app.track}") String track) {
        this.version = version;
        this.track = track;
    }

    @GetMapping
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("application", "cicd-pipeline-demo");
        body.put("version", version);
        body.put("track", track);
        body.put("instance", hostName());
        body.put("status", "UP");
        return body;
    }

    private static String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            String fromEnv = System.getenv("HOSTNAME");
            return fromEnv != null ? fromEnv : "unknown";
        }
    }
}
