package com.spaceapps.aqi.util;

import java.util.List;

public final class AqiBands {

    private record BandInfo(int min, int max, String name, String color, String publicAdvice, String sensitiveAdvice) {
        boolean inRange(int value) {
            return value >= min && value <= max;
        }
    }

    private static final List<BandInfo> BANDS = List.of(
            new BandInfo(0, 50, "Good", "#00B050",
                    "Air quality is satisfactory; outdoor activities are safe.",
                    "Enjoy outdoor activities."),
            new BandInfo(51, 100, "Moderate", "#92D050",
                    "Air quality is acceptable; watch for symptoms if unusually sensitive.",
                    "Sensitive groups should limit prolonged outdoor exertion."),
            new BandInfo(101, 150, "Unhealthy for Sensitive Groups", "#FFC000",
                    "Members of sensitive groups should reduce prolonged outdoor exertion.",
                    "Sensitive groups should avoid strenuous activities outdoors."),
            new BandInfo(151, 200, "Unhealthy", "#FF0000",
                    "Everyone should reduce prolonged outdoor exertion.",
                    "Sensitive groups should stay indoors and keep activity light."),
            new BandInfo(201, 300, "Very Unhealthy", "#7030A0",
                    "Everyone should avoid outdoor exertion.",
                    "Sensitive groups should remain indoors with clean air."),
            new BandInfo(301, Integer.MAX_VALUE, "Hazardous", "#7F0000",
                    "Health warning of emergency conditions; avoid all outdoor activity.",
                    "Sensitive groups should seek shelter in cleaner air immediately.")
    );

    private AqiBands() {
    }

    public static String bandOf(int aqi) {
        return bandInfoOf(aqi).name();
    }

    public static String colorOf(String band) {
        return bandInfoByName(band).color();
    }

    public static String publicAdvice(String band) {
        return bandInfoByName(band).publicAdvice();
    }

    public static String sensitiveAdvice(String band) {
        return bandInfoByName(band).sensitiveAdvice();
    }

    public static String pollutantNote(String pollutant) {
        return switch (pollutant.toUpperCase()) {
            case "NO2" -> "Traffic-related irritant; can trigger asthma.";
            case "O3" -> "Often peaks in afternoon; irritates lungs during exercise.";
            case "PM2_5", "PM2.5" -> "Fine particles; higher risk for heart/lung conditions.";
            case "SO2" -> "Industrial emissions; can cause breathing discomfort.";
            case "CO" -> "Reduces oxygen delivery; avoid heavy exertion.";
            default -> "Monitor local guidance for pollutant impacts.";
        };
    }

    public static String stricterBand(String band) {
        int index = indexOfBand(band);
        if (index < 0) {
            return band;
        }
        return index + 1 < BANDS.size() ? BANDS.get(index + 1).name() : BANDS.get(index).name();
    }

    public static int midpointForBand(String band) {
        BandInfo info = bandInfoByName(band);
        if (info.max() == Integer.MAX_VALUE) {
            return info.min();
        }
        return (info.min() + info.max()) / 2;
    }

    private static BandInfo bandInfoOf(int aqi) {
        return BANDS.stream()
                .filter(b -> b.inRange(aqi))
                .findFirst()
                .orElse(BANDS.get(BANDS.size() - 1));
    }

    private static BandInfo bandInfoByName(String band) {
        return BANDS.stream()
                .filter(b -> b.name().equalsIgnoreCase(band))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown band: " + band));
    }

    private static int indexOfBand(String band) {
        for (int i = 0; i < BANDS.size(); i++) {
            if (BANDS.get(i).name().equalsIgnoreCase(band)) {
                return i;
            }
        }
        return -1;
    }
}
