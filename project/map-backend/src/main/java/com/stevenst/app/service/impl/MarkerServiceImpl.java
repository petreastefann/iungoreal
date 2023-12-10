package com.stevenst.app.service.impl;

import org.springframework.stereotype.Service;

import com.stevenst.app.model.Marker;
import com.stevenst.app.repository.MarkerRepository;
import com.stevenst.app.service.MarkerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarkerServiceImpl implements MarkerService {
	private final MarkerRepository markerRepository;

	@Override
    public Marker addMarker(Marker marker) {
        return markerRepository.save(marker);
    }
}
