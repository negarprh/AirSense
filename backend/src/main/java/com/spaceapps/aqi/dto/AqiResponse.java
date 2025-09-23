package com.spaceapps.aqi.dto;

import java.util.List;

public record AqiResponse(
        String city,
        int aqi,
        String band,
        String color,
        String mainPollutant,
        List<HistoryPoint> history,
        String timestamp
) {
}
