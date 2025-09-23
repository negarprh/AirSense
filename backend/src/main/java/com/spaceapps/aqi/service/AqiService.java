package com.spaceapps.aqi.service;

import com.spaceapps.aqi.dto.AqiResponse;

public interface AqiService {
    AqiResponse fetchNormalizedAqi(String city);
}
