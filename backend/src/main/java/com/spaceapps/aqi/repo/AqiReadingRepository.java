package com.spaceapps.aqi.repo;

import com.spaceapps.aqi.model.AqiReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AqiReadingRepository extends JpaRepository<AqiReading, Long> {
    List<AqiReading> findTop24ByCityOrderByMeasuredAtDesc(String city);
}
