package com.stevenst.lib.model;

import java.time.LocalDateTime;

import com.stevenst.lib.model.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "\"notification\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "\"receiver_id\"", nullable = false)
	private User receiver;

	@ManyToOne(optional = false)
	@JoinColumn(name = "\"emitter_id\"", nullable = false)
	private User emitter;

	@Enumerated(EnumType.STRING)
	@Column(name = "\"type\"", nullable = false)
	private NotificationType type;

	private String description;

	@Column(nullable = false)
	private final LocalDateTime createdAt = LocalDateTime.now();

	@Builder.Default
	@Column(name = "\"read\"", nullable = false)
	private boolean read = false;
}
