package com.spaceapps.aqi.service;

import com.spaceapps.aqi.dto.AdviceResponse;

public interface AdviceService {
    AdviceResponse advise(String city, boolean asthma);
}
