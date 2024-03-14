package com.stevenst.lib.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "\"region\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region implements Serializable {
	@Id
	@Column(nullable = false, unique = true)
	private Long id;

	@Column(nullable = false)
	private String countryName;

	@Column(nullable = false)
	private String name;

	private String code;

	@Column(nullable = false)
	private Double latitude;
	
	@Column(nullable = false)
	private Double longitude;
	
	private String type;
}
