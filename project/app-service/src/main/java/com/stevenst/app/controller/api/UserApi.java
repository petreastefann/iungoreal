package com.stevenst.app.controller.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.stevenst.lib.model.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface UserApi {
	@Operation(summary = "Get User by Username", description = "Retrieve an user by username", tags = "User")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful", content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class)) }),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })

	@GetMapping("/getByUsername")
	public ResponseEntity<User> getUserByUsername(String username);

	@Operation(summary = "Get User by Email", description = "Retrieve an user by email", tags = "User")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful", content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class)) }),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })

	@GetMapping("/getByEmail")
	public ResponseEntity<User> getUserByEmail(String email);
}