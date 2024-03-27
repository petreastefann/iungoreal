package com.stevenst.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stevenst.app.payload.ChatroomPayload;
import com.stevenst.app.service.ChatService;
import com.stevenst.lib.payload.ResponsePayload;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
	private final ChatService chatService;

	@GetMapping("/getFriendsWithNoDmChats")
	public ResponseEntity<List<String>> getFriendsWithoutDmChatrooms(@RequestParam("username") String username) {
		return ResponseEntity.ok(chatService.getFriendsWithoutDmChatrooms(username));
	}

	@PostMapping("/createChatroom")
	public ResponseEntity<ResponsePayload> createChatroom(@RequestParam("username") String username,
			@RequestParam("friendUsername") String friendUsername) {
		return ResponseEntity.ok(chatService.createChatroom(username, friendUsername));
	}

	@GetMapping("/getAllChatroomsOfUser")
	public ResponseEntity<List<ChatroomPayload>> getAllChatroomsOfUser(@RequestParam("username") String username) {
		return ResponseEntity.ok(chatService.getAllChatroomsOfUser(username));
	}
}
