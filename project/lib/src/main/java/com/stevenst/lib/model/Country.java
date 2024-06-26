package com.stevenst.lib.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "\"country\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country implements Serializable{
	@Id
	@Column(nullable = false, unique = true)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false, unique = true)
	private String iso3;

	@Column(nullable = false, unique = true)
	private String iso2;

	private String numericCode;

	private String phoneCode;

	private String capital;

	private String currency;

	private String currencyCode;

	private String nationality;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;
}
