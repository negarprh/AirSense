package com.spaceapps.aqi.service.impl;

import com.spaceapps.aqi.dto.AdviceResponse;
import com.spaceapps.aqi.dto.AqiResponse;
import com.spaceapps.aqi.service.AdviceService;
import com.spaceapps.aqi.service.AqiService;
import com.spaceapps.aqi.util.AqiBands;
import org.springframework.stereotype.Service;

@Service
public class AdviceServiceImpl implements AdviceService {

    private final AqiService aqiService;

    public AdviceServiceImpl(AqiService aqiService) {
        this.aqiService = aqiService;
    }

    @Override
    public AdviceResponse advise(String city, boolean asthma) {
        AqiResponse aqi = aqiService.fetchNormalizedAqi(city);
        String band = aqi.band();
        String advisoryBand = asthma ? AqiBands.stricterBand(band) : band;

        String publicAdvice = AqiBands.publicAdvice(advisoryBand);
        String sensitiveAdvice = AqiBands.sensitiveAdvice(advisoryBand);
        String pollutantNote = AqiBands.pollutantNote(aqi.mainPollutant());

        return new AdviceResponse(
                aqi.city(),
                aqi.aqi(),
                band,
                publicAdvice,
                sensitiveAdvice,
                pollutantNote
        );
    }
}
