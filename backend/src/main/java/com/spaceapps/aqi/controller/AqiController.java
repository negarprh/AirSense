package com.spaceapps.aqi.controller;

import com.spaceapps.aqi.dto.AdviceResponse;
import com.spaceapps.aqi.dto.AqiResponse;
import com.spaceapps.aqi.service.AdviceService;
import com.spaceapps.aqi.service.AqiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AqiController {

    private final AqiService aqiService;
    private final AdviceService adviceService;

    public AqiController(AqiService aqiService, AdviceService adviceService) {
        this.aqiService = aqiService;
        this.adviceService = adviceService;
    }

    @GetMapping("/health")
    @Operation(summary = "Simple health endpoint for infrastructure checks")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/aqi")
    @Operation(summary = "Fetch normalized AQI for a given city")
    public AqiResponse getAqi(
            @RequestParam("city")
            @Parameter(description = "City name to query", example = "Los Angeles") String city) {
        return aqiService.fetchNormalizedAqi(city);
    }

    @GetMapping("/advice")
    @Operation(summary = "Fetch rule-based air quality advice for a city")
    public AdviceResponse getAdvice(
            @RequestParam("city")
            @Parameter(description = "City name to query", example = "Los Angeles") String city,
            @RequestParam(name = "asthma", defaultValue = "false")
            @Parameter(description = "Set true for asthma-sensitive advice") boolean asthma) {
        return adviceService.advise(city, asthma);
    }
}
