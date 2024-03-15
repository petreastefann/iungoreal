package com.stevenst.app.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stevenst.app.repository.CountryRepository;
import com.stevenst.app.repository.RegionRepository;
import com.stevenst.app.service.CountryAndRegionService;
import com.stevenst.app.util.CountryFromJson;
import com.stevenst.app.util.JsonUtil;
import com.stevenst.lib.exception.IgorEntityNotFoundException;
import com.stevenst.lib.exception.IgorIoException;
import com.stevenst.lib.model.Country;
import com.stevenst.lib.model.Region;
import com.stevenst.lib.payload.ResponsePayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CountryAndRegionServiceImpl implements CountryAndRegionService {
	@Value("${app.countries-and-regions-filename}")
	private String COUNTRIES_AND_REGIONS_FILENAME;
	private final RegionRepository regionRepository;
	private final CountryRepository countryRepository;

	@Override
	public List<Region> getAllRegionsByCountry(String countryName) {
		Country country = countryRepository.findByName(countryName);
		if (country == null) {
			throw new IgorEntityNotFoundException("Country not found.");
		}

		return regionRepository.findAllByCountry(country);
	}

	@Override
	public ResponsePayload insertCountryAndRegionsIntoDb(String countryName) {
		Country country = countryRepository.findByName(countryName);
		if (country != null) {
			return ResponsePayload.builder().status(200).message("Country already exists.").build();
		}

		long count = regionRepository.countByCountry(country);

		if (count < 1) {
			try {
				CountryFromJson countryFromJson = JsonUtil.loadCountryAndRegionsFromJsonClasspath(countryName,
						COUNTRIES_AND_REGIONS_FILENAME);
				if (countryFromJson == null) {
					return ResponsePayload.builder().status(404).message("Country not found.").build();
				}
				
				Country countryToInsertIntoDb = countryFromJson.convertToCountry();
				List<Region> regions = countryFromJson.getRegions();
				for (Region region : regions) {
					region.setCountry(countryToInsertIntoDb);
				}

				countryRepository.save(countryToInsertIntoDb);
				regionRepository.saveAll(regions);

				return ResponsePayload.builder().status(200).message("Country and regions inserted successfully.")
						.build();
			} catch (IOException e) {
				throw new IgorIoException(e.getMessage());
			}
		} else {
			return ResponsePayload.builder().status(200).message("Regions already exist.").build();
		}
	}

}
