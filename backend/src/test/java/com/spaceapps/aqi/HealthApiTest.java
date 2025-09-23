package com.spaceapps.aqi;

import com.spaceapps.aqi.controller.AqiController;
import com.spaceapps.aqi.service.AdviceService;
import com.spaceapps.aqi.service.AqiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AqiController.class)
class HealthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AqiService aqiService;

    @MockBean
    private AdviceService adviceService;

    @Test
    void healthEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\"}"));
    }
}
