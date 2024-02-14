package com.stevenst.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stevenst.app.controller.api.UserApi;
import com.stevenst.app.payload.UserPrivatePayload;
import com.stevenst.app.payload.UserPublicPayload;
import com.stevenst.app.service.UserService;
import com.stevenst.lib.payload.ResponsePayload;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController implements UserApi {
	private final UserService userService;

	@GetMapping("/getPublicByUsername")
	public ResponseEntity<UserPublicPayload> getUserPublicByUsername(@RequestParam String username) {
		return ResponseEntity.ok(userService.getUserPublicByUsername(username));
	}

	@GetMapping("/getPrivateByUsername")
	public ResponseEntity<UserPrivatePayload> getUserPrivateByUsername(@RequestParam String username) {
		return ResponseEntity.ok(userService.getUserPrivateByUsername(username));
	}

	@GetMapping("/getByEmail")
	public ResponseEntity<UserPrivatePayload> getUserByEmail(@RequestParam String email) {
		return ResponseEntity.ok(userService.getUserByEmail(email));
	}

	@PutMapping("/saveProfilePicture")
	public ResponseEntity<ResponsePayload> putProfilePicture(@RequestParam String username, MultipartFile file) {
		return ResponseEntity.ok(userService.savePfp(username, file));
	}

	@GetMapping("getProfilePicture")
	public ResponseEntity<String> getProfilePicture(@RequestParam String username) {
		return ResponseEntity.ok(userService.getPfpPreSignedLinkFromS3(username));
	}

	@DeleteMapping("deleteProfilePicture")
	public ResponseEntity<ResponsePayload> deleteProfilePicture(@RequestParam String username) {
		return ResponseEntity.ok(userService.removePfp(username));
	}
}
