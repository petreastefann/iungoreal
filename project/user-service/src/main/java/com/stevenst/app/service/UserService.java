package com.stevenst.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.stevenst.lib.payload.CountryOrRegionPayload;
import com.stevenst.app.payload.UserPrivatePayload;
import com.stevenst.app.payload.UserPublicPayload;
import com.stevenst.lib.payload.ResponsePayload;

public interface UserService {
	UserPublicPayload getUserPublicByUsername(String username);

	UserPrivatePayload getUserPrivateByUsername(String username);

	UserPrivatePayload getUserByEmail(String email);

	String getPfpPreSignedLinkFromS3(String username);

	ResponsePayload savePfp(String username, MultipartFile file);

	ResponsePayload removePfpFromDbAndCloud(String username);

	// countries and regions below --------------------------------------------------------------------------------

	List<CountryOrRegionPayload> getAvailableRegionsForUser(String username);

	CountryOrRegionPayload getCountryOfUser(String username);

	CountryOrRegionPayload getPrimaryRegionOfUser(String username);

	List<CountryOrRegionPayload> getSecondaryRegionsOfUser(String username);

	ResponsePayload setCountryForUser(String username, Long countryId);

	ResponsePayload setPrimaryRegionOfUser(String username, Long regionId);

	ResponsePayload addSecondaryRegionForUser(String username, Long regionId);

	ResponsePayload removeCountryForUser(String username);

	ResponsePayload removePrimaryRegionForUser(String username);

	ResponsePayload removeSecondaryRegionForUser(String username, Long regionId);
}
