package com.stevenst.app.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.stevenst.lib.exception.IgorIoException;
import com.stevenst.lib.exception.IgorUserNotFoundException;
import com.stevenst.lib.payload.ResponsePayload;
import com.stevenst.app.config.AmazonS3Config;
import com.stevenst.app.payload.UserPrivatePayload;
import com.stevenst.app.payload.UserPublicPayload;
import com.stevenst.app.repository.UserRepository;
import com.stevenst.app.service.UserService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private static final String USER_NOT_FOUND = "User not found";
	private final UserRepository userRepository;
	private final S3Client s3Client;
	@Value("${aws.bucketName}")
	private String bucketName;

	@Override
	public UserPublicPayload getUserPublicByUsername(String username) {
		return userRepository.findByUsername(username)
				.map(user -> UserPublicPayload.builder()
						.username(user.getUsername())
						.createdAt(user.getCreatedAt())
						.build())
				.orElseThrow(
						() -> new IgorUserNotFoundException(USER_NOT_FOUND + " (with username: " + username + ")"));
	}

	@Override
	public UserPrivatePayload getUserPrivateByUsername(String username) {
		return userRepository.findByUsername(username)
				.map(user -> UserPrivatePayload.builder()
						.id(user.getId())
						.email(user.getEmail())
						.username(user.getUsername())
						.role(user.getRole())
						.createdAt(user.getCreatedAt())
						.build())
				.orElseThrow(
						() -> new IgorUserNotFoundException(USER_NOT_FOUND + " (with username: " + username + ")"));
	}
	// TODO: users dont need to know their role is of a USER so update the code on front and back to return the role only to admins

	@Override
	public UserPrivatePayload getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.map(user -> UserPrivatePayload.builder()
						.id(user.getId())
						.email(user.getEmail())
						.username(user.getUsername())
						.role(user.getRole())
						.createdAt(user.getCreatedAt())
						.build())
				.orElseThrow(() -> new IgorUserNotFoundException(USER_NOT_FOUND + " (with email: " + email + ")"));
	}

	public ResponsePayload savePfpToS3(String username, MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String folder = username;
		String key = (folder != null && !folder.isEmpty() ? folder : "") + "/profile_picture/" + fileName;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(key)
					.build();

			System.out.println("Uploading " + fileName + " to S3 bucket " + bucketName + " at key " + key);

			return convertAndUploadToS3(putObjectRequest, folder, key, file);
		} catch (S3Exception e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			throw e;
		}
	}

	@Override
	public String getPfpLinkFromS3(String username) {
		String fileName = "Screenshot 2023-07-11 122122.png";

		GetObjectRequest getObjectRequest = createGetObjectRequest(username, fileName);
		return generatePresignedUrl(getObjectRequest);
	}

	// ----------------------------------------------------------------------------------------------------------

	private ResponsePayload convertAndUploadToS3(PutObjectRequest putObjectRequest, String folder,
			String key, MultipartFile file) {
		try (InputStream inputStream = file.getInputStream()) {
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

			return ResponsePayload.builder()
					.status(200)
					.message("File uploaded to S3 bucket " + bucketName + " at key " + key)
					.build();
		} catch (IOException e) {
			System.err.println("Unable to convert MultipartFile to InputStream: " + e.getMessage());
			throw new IgorIoException(e.getMessage());
		}
	}

	private GetObjectRequest createGetObjectRequest(String username, String fileName) {
		String folder = username;
		String key = (folder != null && !folder.isEmpty() ? folder : "") + "/profile_picture/" + fileName;

		return GetObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.build();
	}

	private String generatePresignedUrl(GetObjectRequest getObjectRequest) {
		Duration expiration = Duration.ofHours(1);
		S3Presigner presigner = S3Presigner.builder().region(Region.EU_CENTRAL_1).build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(getObjectRequest)
				.signatureDuration(expiration)
				.build();

		PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
		presigner.close();

		return presignedRequest.url().toString();
	}

}
