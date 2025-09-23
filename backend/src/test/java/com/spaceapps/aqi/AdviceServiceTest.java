package com.spaceapps.aqi;

import com.spaceapps.aqi.dto.AdviceResponse;
import com.spaceapps.aqi.dto.AqiResponse;
import com.spaceapps.aqi.service.AdviceService;
import com.spaceapps.aqi.service.AqiService;
import com.spaceapps.aqi.service.impl.AdviceServiceImpl;
import com.spaceapps.aqi.util.AqiBands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdviceServiceTest {

    @Mock
    private AqiService aqiService;

    private AdviceService adviceService;

    @BeforeEach
    void setUp() {
        adviceService = new AdviceServiceImpl(aqiService);
    }

    @Test
    void returnsAdviceForGivenBand() {
        AqiResponse response = new AqiResponse(
                "Paris",
                120,
                "Unhealthy for Sensitive Groups",
                "#FFC000",
                "PM2_5",
                List.of(),
                "2024-01-01T00:00:00Z"
        );
        when(aqiService.fetchNormalizedAqi("Paris")).thenReturn(response);

        AdviceResponse advice = adviceService.advise("Paris", false);

        assertEquals("Unhealthy for Sensitive Groups", advice.band());
        assertEquals(AqiBands.publicAdvice("Unhealthy for Sensitive Groups"), advice.publicAdvice());
        assertEquals(AqiBands.sensitiveAdvice("Unhealthy for Sensitive Groups"), advice.sensitiveAdvice());
    }

    @Test
    void asthmaRequestsStricterAdvice() {
        AqiResponse response = new AqiResponse(
                "Rome",
                80,
                "Moderate",
                "#92D050",
                "O3",
                List.of(),
                "2024-01-01T00:00:00Z"
        );
        when(aqiService.fetchNormalizedAqi("Rome")).thenReturn(response);

        AdviceResponse advice = adviceService.advise("Rome", true);

        String stricterBand = AqiBands.stricterBand("Moderate");
        assertEquals("Moderate", advice.band());
        assertEquals(AqiBands.publicAdvice(stricterBand), advice.publicAdvice());
        assertEquals(AqiBands.sensitiveAdvice(stricterBand), advice.sensitiveAdvice());
    }
}
