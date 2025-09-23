package com.spaceapps.aqi.dto;

public record AdviceResponse(
        String city,
        int aqi,
        String band,
        String publicAdvice,
        String sensitiveAdvice,
        String pollutantNote
) {
}
