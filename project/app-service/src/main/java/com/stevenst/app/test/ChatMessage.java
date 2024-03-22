package com.stevenst.app.test;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
	private String username;
	private String message;
	private LocalDateTime createdAt = LocalDateTime.now();
}
