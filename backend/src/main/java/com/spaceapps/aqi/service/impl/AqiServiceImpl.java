package com.spaceapps.aqi.service.impl;

import com.spaceapps.aqi.dto.AqiResponse;
import com.spaceapps.aqi.dto.HistoryPoint;
import com.spaceapps.aqi.model.AqiReading;
import com.spaceapps.aqi.repo.AqiReadingRepository;
import com.spaceapps.aqi.service.AqiService;
import com.spaceapps.aqi.service.impl.cache.SimpleCache;
import com.spaceapps.aqi.util.AqiBands;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AqiServiceImpl implements AqiService {

    private static final List<String> POLLUTANTS = List.of("NO2", "O3", "PM2_5", "SO2", "CO");

    private final AqiReadingRepository repository;
    private final SimpleCache<String, AqiResponse> cache;

    public AqiServiceImpl(AqiReadingRepository repository, SimpleCache<String, AqiResponse> cache) {
        this.repository = repository;
        this.cache = cache;
    }

    @Override
    public AqiResponse fetchNormalizedAqi(String city) {
        String normalizedCity = city == null ? "" : city.trim();
        if (normalizedCity.isEmpty()) {
            throw new IllegalArgumentException("City name must not be empty");
        }
        String cacheKey = normalizedCity.toLowerCase(Locale.ROOT) + ":" + LocalDate.now();
        return cache.get(cacheKey).orElseGet(() -> {
            AqiResponse response = generateReading(normalizedCity);
            cache.put(cacheKey, response);
            return response;
        });
    }

    private AqiResponse generateReading(String city) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int aqi = random.nextInt(50, 181);
        String pollutant = POLLUTANTS.get(random.nextInt(POLLUTANTS.size()));
        Instant now = Instant.now();

        AqiReading reading = new AqiReading();
        reading.setCity(city);
        reading.setAqi(aqi);
        reading.setMainPollutant(pollutant);
        reading.setMeasuredAt(now);
        repository.save(reading);

        List<HistoryPoint> history = buildHistory(city, now, aqi);
        String band = AqiBands.bandOf(aqi);
        String color = AqiBands.colorOf(band);

        return new AqiResponse(
                city,
                aqi,
                band,
                color,
                pollutant,
                history,
                now.toString()
        );
    }

    private List<HistoryPoint> buildHistory(String city, Instant now, int latestAqi) {
        List<AqiReading> stored = new ArrayList<>(repository.findTop24ByCityOrderByMeasuredAtDesc(city));
        if (stored.isEmpty()) {
            return generateSyntheticHistory(now, latestAqi);
        }

        stored.sort(Comparator.comparing(AqiReading::getMeasuredAt));
        List<HistoryPoint> history = new ArrayList<>();
        for (AqiReading reading : stored) {
            history.add(new HistoryPoint(reading.getMeasuredAt().toString(), reading.getAqi()));
        }

        if (history.size() >= 24) {
            return history.subList(history.size() - 24, history.size());
        }

        int needed = 24 - history.size();
        List<HistoryPoint> filler = prependSyntheticHistory(history.get(0), needed);
        filler.addAll(history);
        return filler;
    }

    private List<HistoryPoint> generateSyntheticHistory(Instant now, int latestAqi) {
        List<HistoryPoint> history = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int current = latestAqi;
        for (int hoursAgo = 23; hoursAgo >= 1; hoursAgo--) {
            current = adjustWithinBounds(current + random.nextInt(-5, 6));
            Instant timestamp = now.minus(Duration.ofHours(hoursAgo));
            history.add(new HistoryPoint(timestamp.toString(), current));
        }
        history.add(new HistoryPoint(now.toString(), latestAqi));
        return history;
    }

    private List<HistoryPoint> prependSyntheticHistory(HistoryPoint reference, int count) {
        List<HistoryPoint> filler = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Instant cursor = Instant.parse(reference.timestamp());
        int current = reference.aqi();
        for (int i = count; i >= 1; i--) {
            cursor = cursor.minus(Duration.ofHours(1));
            current = adjustWithinBounds(current + random.nextInt(-4, 1));
            filler.add(0, new HistoryPoint(cursor.toString(), current));
        }
        return filler;
    }

    private int adjustWithinBounds(int value) {
        return Math.max(5, Math.min(value, 400));
    }
}
