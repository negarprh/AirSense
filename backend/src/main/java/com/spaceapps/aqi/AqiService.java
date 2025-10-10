package com.spaceapps.aqi;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
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

    private static final String DEFAULT_PM25_UNIT = "ug/m3";
    private static final int PM25_ID = 2;

    // API caps radius at 25 km. We’ll retry with bigger circles by issuing
    // another query centered on the same coords and let OpenAQ pick farther stations.
    private static final int[] RADII = {25_000, 75_000, 150_000}; // meters (25 km hard cap per call)
    private static final int LOOKBACK_DAYS_PRIMARY = 60;
    private static final int LOOKBACK_DAYS_FALLBACK = 120;

    private final RestClient openaq;
    private final RestClient geocode;
    private final ObjectMapper mapper = new ObjectMapper();

    public AqiService(
            @Value("${OPENAQ_API_KEY:}") String apiKey,
            @Value("${GEOCODING_USER_AGENT:EarthDataAQI/1.0 (contact@example.com)}") String ua
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAQ_API_KEY environment variable is required.");
        }
        this.openaq = RestClient.builder()
                .baseUrl("https://api.openaq.org/v3")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-Key", apiKey.trim())
                .build();
        this.geocode = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, ua)
                .build();
    }

    /* ========================= PUBLIC API ========================= */

    public ObjectNode fetchByCity(String cityQuery) {
        String q = cityQuery == null ? "" : cityQuery.trim();
        if (q.isEmpty()) return message("City is required.");

        Geo g = geocodeCity(q);
        if (g == null) return message("Unable to geocode the requested city.");

        ObjectNode row = pm25LatestFromNearbySensors(g.lat, g.lon, LOOKBACK_DAYS_PRIMARY);
        if (row == null) row = pm25LatestFromNearbySensors(g.lat, g.lon, LOOKBACK_DAYS_FALLBACK);

        if (row == null || !row.hasNonNull("value")) return message("No PM2.5 data available for this city.");

        double pm25 = row.path("value").asDouble();
        int aqi = aqiFromPm25(pm25);

        ObjectNode out = mapper.createObjectNode();
        out.put("query", q);
        out.put("resolved", g.display);
        out.put("pm25", pm25);
        out.put("unit", row.path("unit").asText(DEFAULT_PM25_UNIT));
        out.put("observedUtc", row.path("datetime").path("utc").asText(""));
        out.put("aqi", aqi);
        out.put("aqi_category", aqiCategory(aqi));
        out.put("health_advice", adviceForAqi(aqi));
        // Optional: include station info if present
        if (row.has("coordinates")) {
            out.set("station", row.get("coordinates"));
        } else if (row.has("locationName")) {
            out.put("station", row.path("locationName").asText());
        }
        if (row.has("locationId")) out.put("stationId", row.path("locationId").asInt());
        if (row.has("sensorId")) out.put("sensorId", row.path("sensorId").asInt());
        return out;
    }

    public ObjectNode fetchLocation(int locationId) {
        try {
            ObjectNode row = latestPm25ForLocation(
                    locationId, Instant.now().minus(Duration.ofDays(LOOKBACK_DAYS_PRIMARY)));
            if (row == null) {
                row = latestPm25ForLocation(
                        locationId, Instant.now().minus(Duration.ofDays(LOOKBACK_DAYS_FALLBACK)));
            }
            if (row == null) return message("No PM2.5 data available for this location.");

            double pm25 = row.path("value").asDouble();
            int aqi = aqiFromPm25(pm25);

            ObjectNode out = mapper.createObjectNode();
            out.put("locationId", locationId);
            out.put("pm25", pm25);
            out.put("unit", row.path("unit").asText(DEFAULT_PM25_UNIT));
            out.put("observedUtc", row.path("datetime").path("utc").asText(""));
            out.put("aqi", aqi);
            out.put("aqi_category", aqiCategory(aqi));
            out.put("health_advice", adviceForAqi(aqi));
            if (row.has("coordinates")) {
                out.set("station", row.get("coordinates"));
            } else if (row.has("locationName")) {
                out.put("station", row.path("locationName").asText());
            }
            if (row.has("locationName")) out.put("locationName", row.path("locationName").asText());
            if (row.has("sensorId")) out.put("sensorId", row.path("sensorId").asInt());
            return out;
        } catch (RestClientResponseException e) {
            ObjectNode err = handleRestException(e);
            return err != null ? err : message("Upstream error from OpenAQ (" + e.getStatusCode().value() + ").");
        } catch (Exception e) {
            return message("Unexpected error: " + e.getMessage());
        }
    }

    /* ========================= NEARBY SENSOR LOOKUP ========================= */

    private ObjectNode pm25LatestFromNearbySensors(double lat, double lon, int lookbackDays) {
        ArrayNode locs = findNearbyLocations(lat, lon, 60, 25_000); // max per docs
        if (locs == null || locs.isEmpty()) return null;

        Instant threshold = Instant.now().minus(Duration.ofDays(lookbackDays));
        Instant bestTs = null;
        ObjectNode best = null;

        for (JsonNode loc : locs) {
            int locationId = loc.path("id").asInt(-1);
            if (locationId <= 0) continue;

            ObjectNode candidate = latestPm25ForLocation(locationId, threshold);
            if (candidate == null || !candidate.hasNonNull("value")) continue;

            if (loc.hasNonNull("name")) candidate.put("locationName", loc.path("name").asText());
            if (loc.has("coordinates") && !candidate.has("coordinates")) {
                candidate.set("coordinates", loc.get("coordinates"));
            }
            if (candidate.path("locationId").asInt(-1) <= 0) {
                candidate.put("locationId", locationId);
            }

            Instant ts = parseUtc(candidate.path("datetime").path("utc").asText(null));
            if (ts == null) continue;

            if (bestTs == null || ts.isAfter(bestTs)) {
                bestTs = ts;
                best = candidate;
            }
        }
        return best;
    }

    private ObjectNode latestPm25ForLocation(int locationId, Instant threshold) {
        String url = UriComponentsBuilder.fromPath("/locations/{id}/sensors")
                .queryParam("parameter_id", PM25_ID)
                .queryParam("limit", 6)
                .buildAndExpand(locationId)
                .toUriString();
        try {
            JsonNode root = openaq.get().uri(url).retrieve().body(JsonNode.class);
            ArrayNode sensors = asArray(root, "results");
            if (sensors == null || sensors.isEmpty()) return null;

            Instant bestTs = null;
            ObjectNode best = null;

            for (JsonNode sensor : sensors) {
                JsonNode parameter = sensor.path("parameter");
                if (parameter.path("id").asInt(-1) != PM25_ID) continue;

                JsonNode latest = sensor.path("latest");
                double value = latest.path("value").asDouble(Double.NaN);
                String utc = latest.path("datetime").path("utc").asText(null);
                Instant ts = parseUtc(utc);

                ObjectNode row;
                if (Double.isNaN(value) || ts == null || (threshold != null && ts.isBefore(threshold))) {
                    row = fetchLatestMeasurementForSensor(sensor.path("id").asInt(-1), threshold);
                    if (row == null) continue;
                    utc = row.path("datetime").path("utc").asText(null);
                    ts = parseUtc(utc);
                    if (ts == null || (threshold != null && ts.isBefore(threshold))) continue;
                } else {
                    row = mapper.createObjectNode();
                    row.put("value", value);
                    row.put("unit", parameter.path("units").asText(DEFAULT_PM25_UNIT));
                    ObjectNode dt = mapper.createObjectNode();
                    dt.put("utc", utc != null ? utc : "");
                    row.set("datetime", dt);
                    JsonNode coords = latest.path("coordinates");
                    if (coords != null && coords.isObject()) row.set("coordinates", coords);
                }

                row.put("sensorId", sensor.path("id").asInt());
                if (sensor.hasNonNull("name")) row.put("sensorName", sensor.path("name").asText());
                row.put("locationId", locationId);
                if (sensor.hasNonNull("locationName")) row.put("locationName", sensor.path("locationName").asText());
                if (!row.has("unit")) row.put("unit", parameter.path("units").asText(DEFAULT_PM25_UNIT));

                if (bestTs == null || ts.isAfter(bestTs)) {
                    bestTs = ts;
                    best = row;
                }
            }
            return best;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 429) throw e;
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private ArrayNode findNearbyLocations(double lat, double lon, int limit, int radiusMeters) {
        String coords = String.format(Locale.US, "%.6f,%.6f", lat, lon); // lat,lon
        String url = UriComponentsBuilder.fromPath("/locations")
                .queryParam("coordinates", coords)
                .queryParam("radius", radiusMeters) // must be <= 25_000 per call
                .queryParam("limit", limit)
                .queryParam("parameter_id", PM25_ID)
                .queryParam("order_by", "distance")
                .queryParam("sort", "asc")
                .build().toUriString();
        try {
            JsonNode root = openaq.get().uri(url).retrieve().body(JsonNode.class);
            return asArray(root, "results");
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 429) throw e;
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private ObjectNode fetchLatestMeasurementForSensor(int sensorId, Instant threshold) {
        if (sensorId <= 0) return null;

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/sensors/{id}/measurements")
                .queryParam("limit", 1)
                .queryParam("order_by", "datetime")
                .queryParam("sort", "desc");
        if (threshold != null) builder.queryParam("date_from", threshold.toString());

        String url = builder.buildAndExpand(sensorId).toUriString();
        try {
            JsonNode root = openaq.get().uri(url).retrieve().body(JsonNode.class);
            ArrayNode res = asArray(root, "results");
            if (res == null || res.isEmpty()) return null;

            JsonNode measurement = res.get(0);
            double value = measurement.path("value").asDouble(Double.NaN);
            if (Double.isNaN(value)) return null;

            ObjectNode row = mapper.createObjectNode();
            row.put("value", value);

            JsonNode parameter = measurement.path("parameter");
            row.put("unit", parameter.path("units").asText(DEFAULT_PM25_UNIT));

            String utc = null;
            JsonNode period = measurement.path("period");
            if (period.has("datetimeTo")) utc = period.path("datetimeTo").path("utc").asText(null);
            if (utc == null && period.has("datetimeFrom")) utc = period.path("datetimeFrom").path("utc").asText(null);
            if (utc == null) utc = measurement.path("datetime").path("utc").asText(null);

            ObjectNode dt = mapper.createObjectNode();
            dt.put("utc", utc != null ? utc : "");
            row.set("datetime", dt);

            JsonNode coords = measurement.path("coordinates");
            if (coords != null && coords.isObject()) row.set("coordinates", coords);

            return row;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 429) throw e;
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }

    /* ========================= GEOCODING ========================= */

    private static final class Geo {
        final double lat, lon; final String display;
        Geo(double lat, double lon, String display) { this.lat = lat; this.lon = lon; this.display = display; }
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
            if (!(root instanceof ArrayNode arr) || arr.isEmpty()) return null;

            JsonNode n = arr.get(0);
            double lat = Double.parseDouble(n.path("lat").asText());
            double lon = Double.parseDouble(n.path("lon").asText());
            String display = n.path("display_name").asText(city);
            return new Geo(lat, lon, display);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 429) throw e;
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }

    /* ========================= BUILDERS & UTIL ========================= */

    private ArrayNode asArray(JsonNode root, String field) {
        if (root == null) return null;
        JsonNode node = root.get(field);
        return (node instanceof ArrayNode) ? (ArrayNode) node : null;
    }

    private Instant parseUtc(String s) {
        try { return (s == null || s.isBlank()) ? null : Instant.parse(s); }
        catch (Exception e) { return null; }
    }

    private ObjectNode message(String msg) {
        return mapper.createObjectNode().put("message", msg);
    }

    private ObjectNode handleRestException(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == 401) return message("Invalid OpenAQ API key.");
        if (status == 429) return message("Rate limited by OpenAQ. Try again soon.");
        return null;
    }

    /* ========================= AQI (US EPA PM2.5) ========================= */

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
