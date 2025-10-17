package com.spaceapps.aqi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.spaceapps.aqi.AqiService;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
// @CrossOrigin(origins = "http://airsense-app.s3-website.ca-central-1.amazonaws.com")
public class AqiController {
    private final AqiService service;
    public AqiController(AqiService service) { this.service = service; }

    @GetMapping("/aqi")
    public ResponseEntity<JsonNode> getAqi(@RequestParam String city) {
        if (!StringUtils.hasText(city)) return ResponseEntity.badRequest().build();
        JsonNode body = service.fetchByCity(city);
        HttpStatus status = body.has("message") ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    @GetMapping("/forecast")
    public ResponseEntity<JsonNode> getForecast(@RequestParam String city) {
        if (!StringUtils.hasText(city)) return ResponseEntity.badRequest().build();
        JsonNode body = service.fetchForecastByCity(city);
        HttpStatus status = body.has("message") ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(body);
    }
}



