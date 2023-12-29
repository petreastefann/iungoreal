package com.stevenst.app.service.impl;

import com.stevenst.app.exception.IgorAuthenticationException;
import com.stevenst.app.payload.AuthRequest;
import com.stevenst.app.payload.AuthResponse;
import com.stevenst.app.payload.RegisterRequest;
import com.stevenst.lib.model.Role;
import com.stevenst.lib.model.User;
import com.stevenst.app.repository.AuthRepository;
import com.stevenst.app.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
	private final AuthRepository authRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtServiceImpl jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthResponse register(RegisterRequest request) {
		if (request.getEmail() == null || request.getEmail().isEmpty() || request.getPassword() == null
				|| request.getPassword().isEmpty() || request.getUsername() == null
				|| request.getUsername().isEmpty()) {
			throw new IgorAuthenticationException("Credentials cannot be empty");
		}

		Optional<User> existingUser = authRepository.findByEmail(request.getEmail());
		if (existingUser.isPresent()) {
			throw new IgorAuthenticationException("Email already taken");
		}

		existingUser = authRepository.findByUsername(request.getUsername());
		if (existingUser.isPresent()) {
			throw new IgorAuthenticationException("Username already taken");
		}

		var user = User.builder()
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.username(request.getUsername())
				.role(Role.USER)
				.build();

		authRepository.save(user);
		var jwtToken = jwtService.generateToken(user);

		return AuthResponse.builder()
				.token(jwtToken)
				.build();
	}

	public AuthResponse login(AuthRequest request) {
		if (request.getEmail() == null || request.getEmail().isEmpty() || request.getPassword() == null
				|| request.getPassword().isEmpty()) {
			throw new IgorAuthenticationException("Credentials cannot be empty");
		}

		var user = authRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new IgorAuthenticationException("User not found"));
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (AuthenticationException e) {
			throw new IgorAuthenticationException("Invalid credentials");
		}

		var jwtToken = jwtService.generateToken(user);

		return AuthResponse.builder()
				.token(jwtToken)
				.build();
	}
}
