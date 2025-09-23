package com.spaceapps.aqi.client.nasa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class EarthdataClient {
    private final WebClient cmr = WebClient.builder()
            .baseUrl("https://cmr.earthdata.nasa.gov/search")
            .defaultHeader("Authorization", "Bearer " + System.getenv("EDL_TOKEN"))
            .build();

    private final WebClient harmony = WebClient.builder()
            .baseUrl("https://harmony.earthdata.nasa.gov")
            .defaultHeader("Authorization", "Bearer " + System.getenv("EDL_TOKEN"))
            .build();

    @Value("${app.tempoShortName:${TEMPO_COLLECTION_SHORTNAME:TEMPO_NO2_L3}}")
    private String tempoShortName;

    // 1) Find latest TEMPO granule over bbox (lonW,latS,lonE,latN) in last 24h
    public Mono<Map> searchTempoGranules(double lonW, double latS, double lonE, double latN) {
        String temporal = Instant.now().minus(24, ChronoUnit.HOURS).toString() + "," + Instant.now().toString();
        return cmr.get()
                .uri(uri -> uri.path("/granules.json")
                        .queryParam("short_name", tempoShortName)
                        .queryParam("temporal", temporal)
                        .queryParam("bounding_box", String.format("%f,%f,%f,%f", lonW, latS, lonE, latN))
                        .queryParam("sort_key", "-start_date")
                        .queryParam("page_size", "1")
                        .build())
                .retrieve().bodyToMono(Map.class);
    }

    // 2) Submit a Harmony job to subset to bbox + variables; returns job location
    public Mono<String> submitHarmonyJob(String collectionConceptId, double lonW, double latS, double lonE, double latN,
            String variablesCsv) {
        Map<String, Object> req = Map.of(
                "resources", new String[] { "cmr:" + collectionConceptId },
                "format", "application/x-netcdf4",
                "subset", Map.of(
                        "bbox", String.format("%f,%f,%f,%f", lonW, latS, lonE, latN),
                        "variables", variablesCsv));
        return harmony.post()
                .uri("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchangeToMono(resp -> Mono.justOrEmpty(resp.headers().asHttpHeaders().getFirst("location")));
    }

    // 3) Poll Harmony job until done; returns a download URL
    public Mono<Map> getHarmonyJob(String jobLocation) {
        return harmony.get().uri(jobLocation.replace("https://harmony.earthdata.nasa.gov", ""))
                .retrieve().bodyToMono(Map.class);
    }
}
