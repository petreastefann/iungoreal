package com.stevenst.app.payload;

import java.time.LocalDateTime;
import java.util.List;

import com.stevenst.app.model.ChatroomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomPayload {
	private Long id;
	private String name;
	private ChatroomType type;
	private LocalDateTime lastMessageTime;
	private List<String> participantsUsernames;
}