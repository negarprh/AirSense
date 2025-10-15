package com.spaceapps.aqi;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Service
public class AqiService {

    private static final String DEFAULT_PM25_UNIT = "µg/m³";

    private final RestClient http;     // generic HTTP client
    private final RestClient geocode;  // Nominatim
    private final ObjectMapper mapper = new ObjectMapper();
    private final String owApiKey;

    public AqiService(
            @Value("${OPENWEATHER_API_KEY:}") String owKey,
            @Value("${GEOCODING_USER_AGENT:AirSense/1.0 (contact@example.com)}") String ua
    ) {
        if (owKey == null || owKey.isBlank()) {
            throw new IllegalStateException("OPENWEATHER_API_KEY environment variable is required.");
        }
        this.owApiKey = owKey.trim();

        this.http = RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.geocode = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, ua)
                .build();
    }

    /* ========================= PUBLIC API ========================= */

        @Cacheable(
        value = "aqiByCity",
        key = "T(org.springframework.util.StringUtils).trimAllWhitespace(#cityQuery).toLowerCase()",
        unless = "#result == null || #result.has('message')" // don't cache errors
    )


    public ObjectNode fetchByCity(String cityQuery) {
        String q = cityQuery == null ? "" : cityQuery.trim();
        if (q.isEmpty()) return message("City is required.");

        Geo g = geocodeCity(q);
        if (g == null) return message("City not found. Please check the spelling or try a nearby major city.");

        ObjectNode row = fetchFromOpenWeather(g.lat, g.lon);
        if (row == null) return message("No air quality data available for this city.");

        double pm25 = row.path("pm25").asDouble();
        int aqi = aqiFromPm25(pm25); // keep US EPA 0–500 scale for your UI

        ObjectNode out = mapper.createObjectNode();
        out.put("query", q);
        out.put("resolved", g.display);
        out.put("pm25", pm25);
        out.put("unit", DEFAULT_PM25_UNIT);
        out.put("observedUtc", row.path("observedUtc").asText(""));
        out.put("aqi", aqi);
        out.put("aqi_category", aqiCategory(aqi));
        out.put("health_advice", adviceForAqi(aqi));
        // optional extras
        out.put("country", g.country != null ? g.country : "");
        return out;
    }

    public ObjectNode fetchLocation(int locationId) {
        // Not applicable with OpenWeather; keep for compatibility or remove endpoint.
        return message("Lookup by OpenAQ locationId is not supported with OpenWeather.");
    }

    /* ========================= OpenWeather fetch ========================= */

    private ObjectNode fetchFromOpenWeather(double lat, double lon) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.openweathermap.org/data/2.5/air_pollution")
                    .queryParam("lat", String.format(Locale.US, "%.6f", lat))
                    .queryParam("lon", String.format(Locale.US, "%.6f", lon))
                    .queryParam("appid", owApiKey)
                    .queryParam("_ts", System.currentTimeMillis()) // cache buster
                    .toUriString();

            JsonNode root = http.get().uri(url).retrieve().body(JsonNode.class);
            ArrayNode list = (root != null && root.get("list") instanceof ArrayNode) ? (ArrayNode) root.get("list") : null;
            if (list == null || list.isEmpty()) return null;

            JsonNode entry = list.get(0);
            JsonNode comps = entry.path("components");
            if (comps == null || comps.isMissingNode()) return null;

            double pm25 = comps.path("pm2_5").asDouble(Double.NaN);
            if (Double.isNaN(pm25)) return null;

            long dt = entry.path("dt").asLong(0L);
            String isoUtc = dt > 0
                    ? Instant.ofEpochSecond(dt).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
                    : "";

            ObjectNode row = mapper.createObjectNode();
            row.put("pm25", pm25);
            row.put("observedUtc", isoUtc);
            return row;
        } catch (RestClientResponseException e) {
            return message("OpenWeather error: " + e.getStatusCode().value());
        } catch (Exception e) {
            return null;
        }
    }

    /* ========================= Geocoding ========================= */

    private static final class Geo {
        final double lat, lon; final String display; final String country;
        Geo(double lat, double lon, String display, String country) { this.lat = lat; this.lon = lon; this.display = display; this.country = country; }
    }

    private Geo geocodeCity(String city) {
        try {
            String url = UriComponentsBuilder.fromPath("/search")
                    .queryParam("q", city)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("addressdetails", 1)
                    .build().toUriString();

            JsonNode root = geocode.get().uri(url).retrieve().body(JsonNode.class);

            if (!(root instanceof ArrayNode arr) || arr.isEmpty()) {
                
                return null;
            }

            JsonNode n = arr.get(0);
            double lat = Double.parseDouble(n.path("lat").asText());
            double lon = Double.parseDouble(n.path("lon").asText());
            String display = n.path("display_name").asText(city);
            String country = n.path("address").path("country").asText("");
            return new Geo(lat, lon, display, country);
        } catch (Exception e) {
            return null;
        }
    }


        

    // add method in AqiService
    public ObjectNode fetchForecastByCity(String cityQuery) {
        String q = cityQuery == null ? "" : cityQuery.trim();
        if (q.isEmpty()) return message("City is required.");

        Geo g = geocodeCity(q);
        if (g == null) return message("City not found. Please enter a valid city name.");

        try {
            String url = UriComponentsBuilder
                .fromHttpUrl("https://api.openweathermap.org/data/2.5/air_pollution/forecast")
                .queryParam("lat", String.format(Locale.US, "%.6f", g.lat))
                .queryParam("lon", String.format(Locale.US, "%.6f", g.lon))
                .queryParam("appid", owApiKey) // use the same field you used for current data
                .toUriString();

            JsonNode root = http.get().uri(url).retrieve().body(JsonNode.class);
            ArrayNode list = (root != null && root.get("list") instanceof ArrayNode) ? (ArrayNode) root.get("list") : null;
            if (list == null || list.isEmpty()) return message("No forecast available.");

            ArrayNode points = mapper.createArrayNode();
            for (JsonNode n : list) {
                long dt = n.path("dt").asLong(0);
                double pm25 = n.path("components").path("pm2_5").asDouble(Double.NaN);
                if (dt == 0 || Double.isNaN(pm25)) continue;
                int aqi = aqiFromPm25(pm25); // keep US EPA 0–500 for UI
                ObjectNode p = mapper.createObjectNode();
                p.put("t", Instant.ofEpochSecond(dt).toString());
                p.put("pm25", pm25);
                p.put("aqi", aqi);
                points.add(p);
            }
            if (points.isEmpty()) return message("No forecast available.");

            ObjectNode out = mapper.createObjectNode();
            out.put("query", q);
            out.put("resolved", g.display);
            out.set("points", points);
            return out;
        } catch (Exception e) {
            return message("OpenWeather forecast error.");
        }
    }


    /* ========================= Util & AQI ========================= */

    private ObjectNode message(String msg) {
        return mapper.createObjectNode().put("message", msg);
    }

    // US EPA PM2.5 → AQI (0–500)
    private int aqiFromPm25(double concentration) {
        double c = Math.floor(concentration * 10.0) / 10.0;
        double[] cLow  = { 0.0, 12.1, 35.5, 55.5, 150.5, 250.5, 350.5 };
        double[] cHigh = {12.0, 35.4, 55.4, 150.4, 250.4, 350.4, 500.4};
        int[]    iLow  = {   0,   51,  101,  151,   201,   301,   401  };
        int[]    iHigh = {  50,  100,  150,  200,   300,   400,   500  };

        if (c >= 500.5) return 500;
        for (int i = 0; i < cLow.length; i++) {
            if (c <= cHigh[i]) {
                double ih = iHigh[i], il = iLow[i], ch = cHigh[i], cl = cLow[i];
                int aqi = (int) Math.round(((ih - il) / (ch - cl)) * (c - cl) + il);
                return Math.max(0, Math.min(500, aqi));
            }
        }
        return 0;
    }

    private String aqiCategory(int aqi) {
        if (aqi <= 50)  return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    private String adviceForAqi(int aqi) {
        if (aqi <= 50)  return "Good - air quality is satisfactory.";
        if (aqi <= 100) return "Moderate - unusually sensitive people should consider limiting prolonged exertion.";
        if (aqi <= 150) return "USG - sensitive groups should reduce prolonged or heavy exertion.";
        if (aqi <= 200) return "Unhealthy - everyone should consider limiting outdoor activities.";
        if (aqi <= 300) return "Very Unhealthy - avoid strenuous outdoor activities.";
        return "Hazardous - remain indoors and follow health guidance.";
    }
}
