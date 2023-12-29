package com.stevenst.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stevenst.app.controller.api.UserApi;
import com.stevenst.app.service.UserService;
import com.stevenst.lib.model.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController implements UserApi {
	private final UserService userService;

	@GetMapping("/getByUsername")
	public ResponseEntity<User> getUserByUsername(@RequestParam String username) {
		return ResponseEntity.ok(userService.getUserByUsername(username));
	}

	@GetMapping("/getByEmail")
	public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
		return ResponseEntity.ok(userService.getUserByEmail(email));
	}
}
