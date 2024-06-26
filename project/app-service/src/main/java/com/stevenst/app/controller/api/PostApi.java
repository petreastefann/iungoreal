package com.stevenst.app.controller.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.stevenst.app.payload.PostPayload;
import com.stevenst.lib.payload.ResponsePayload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface PostApi {
	@Operation(summary = "Create a post", description = "Create a post for an user", tags = "Post")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful", content = {
					@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResponsePayload.class)) })
	})
	public ResponseEntity<ResponsePayload> createPost(String authorUsername, String title, String description,
			List<MultipartFile> files);
}
