package com.stevenst.app.controller;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jayway.jsonpath.JsonPath;
import com.stevenst.app.model.FriendRequests;
import com.stevenst.app.model.Friendships;
import com.stevenst.lib.payload.ResponsePayload;
import com.stevenst.app.repository.FriendRequestsRepository;
import com.stevenst.app.repository.FriendshipsRepository;
import com.stevenst.app.repository.NotificationRepository;
import com.stevenst.app.repository.UserRepository;
import com.stevenst.lib.model.User;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendsControllerIntegrationTest {
	private Server server;
	private static final User userAndrew = User.builder()
			.email("andrew_email123")
			.password("andrew_password123")
			.username("andrew_username123").build();
	private static final User userBobby = User.builder()
			.email("bobby_email123")
			.password("bobby_password123")
			.username("bobby_username123").build();

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private FriendshipsRepository friendshipsRepository;
	@Autowired
	private FriendRequestsRepository friendRequestsRepository;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private UserRepository userRepository;

	@BeforeAll
	void init() throws Exception {
		server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
		server.start();
	}

	@AfterAll
	void exit() throws Exception {
		server.stop();
	}

	@BeforeEach
	void setUp() throws Exception {
		insertUserIntoDB(userAndrew);
		insertUserIntoDB(userBobby);
	}

	@AfterEach
	void tearDown() throws SQLException {
		cleanDB();
	}

	@Test
	void sendFriendRequest() throws Exception {
		MvcResult result = mockMvc.perform(
				post("/api/friend/sendRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(200, response.getStatus());
		assertEquals("Friend request sent successfully (from " + userAndrew.getUsername() + " to "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void checkFriendRequest() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				get("/api/friend/checkRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(200, response.getStatus());
		assertEquals("Friend request found (from " + userAndrew.getUsername() + " to "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void checkFriendRequest_friendRequestNotFound() throws Exception {
		MvcResult result = mockMvc.perform(
				get("/api/friend/checkRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(404, response.getStatus());
		assertEquals(
				"No friend request found (from " + userAndrew.getUsername() + " to " + userBobby.getUsername() + ")",
				response.getMessage());
	}

	@Test
	void checkFriendship() throws Exception {
		addFriendship(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				get("/api/friend/checkFriendship?user1=" + userAndrew.getUsername() + "&user2="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(200, response.getStatus());
		assertEquals("Friendship found (between " + userAndrew.getUsername() + " and "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void checkFriendship_friendshipNotFound() throws Exception {
		MvcResult result = mockMvc.perform(
				get("/api/friend/checkFriendship?user1=" + userAndrew.getUsername() + "&user2="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(404, response.getStatus());
		assertEquals(
				"No friendship found (between " + userAndrew.getUsername() + " and " + userBobby.getUsername() + ")",
				response.getMessage());
	}

	@Test
	void getAllFriendsUsernames() throws Exception {
		User userJoe = User.builder()
				.email("blob_joe_email123")
				.password("blob_joe_password123")
				.username("blob_joe_username123").build();
		insertUserIntoDB(userJoe);
		addFriendship(userAndrew, userJoe);
		addFriendship(userBobby, userJoe);

		MvcResult result = mockMvc.perform(
				get("/api/friend/getAllFriendsUsernames?username=" + userJoe.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		var responseJson = JsonPath.parse(result.getResponse().getContentAsString());
		int nbOfFriendsReturned = responseJson.read("$.length()");
		List<String> friendsUsernames = responseJson.read("$");

		assertEquals(2, nbOfFriendsReturned);
		assertTrue(friendsUsernames.contains(userBobby.getUsername()));
		assertTrue(friendsUsernames.contains(userAndrew.getUsername()));
	}

	@Test
	void getAllFriendsUsernames_noFriends() throws Exception {
		MvcResult result = mockMvc.perform(
				get("/api/friend/getAllFriendsUsernames?username=" + userAndrew.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		var responseJson = JsonPath.parse(result.getResponse().getContentAsString());
		int nbOfFriendsReturned = responseJson.read("$.length()");
		List<String> friendsUsernames = responseJson.read("$");

		assertEquals(0, nbOfFriendsReturned);
		assertTrue(friendsUsernames.isEmpty());
	}

	@Test
	void acceptFriendRequest() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				put("/api/friend/acceptRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(200, response.getStatus());
		assertEquals("Friend request accepted successfully (from " + userAndrew.getUsername() + " accepted by "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void cancelFriendRequest() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				delete("/api/friend/cancelRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(200, response.getStatus());
		assertEquals("Friend request canceled successfully (from " + userAndrew.getUsername() + " to "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void declineFriendRequest() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				delete("/api/friend/declineRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(200, response.getStatus());
		assertEquals("Friend request declined successfully (from " + userAndrew.getUsername() + " to "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void unfriend() throws Exception {
		addFriendship(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				delete("/api/friend/unfriend?unfriender=" + userAndrew.getUsername() + "&unfriended="
						+ userBobby.getUsername()))
				.andExpect(status().isOk())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(200, response.getStatus());
		assertEquals("Unfriend successfully done (" + userAndrew.getUsername() + " unfriended "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	// tests for unsuccessfull requests

	@ParameterizedTest
	@CsvSource({
			"POST, /sendRequest, sender, receiver",
			"GET, /checkRequest, sender, receiver",
			"GET, /checkFriendship, user1, user2",
			"GET, /getAllFriendsUsernames, username,",
			"PUT, /acceptRequest, sender, receiver",
			"DELETE, /cancelRequest, sender, receiver",
			"DELETE, /declineRequest, sender, receiver",
			"DELETE, /unfriend, unfriender, unfriended",
	})
	void allEndpoints_userDoesntExist(String requestType, String endpoint, String param1, String param2)
			throws Exception {
		String requestUrl1 = "/api/friend" + endpoint + "?";
		String requestUrl2 = "/api/friend" + endpoint + "?";
		if (endpoint.equals("/getAllFriendsUsernames")) {
			requestUrl1 += param1 + "=" + "doesntexist";
			requestUrl2 += param1 + "=" + "doesntexist";
		} else {
			requestUrl1 += param1 + "=" + "doesntexist" + "&" + param2 + "=" + userBobby.getUsername();
			requestUrl2 += param1 + "=" + userAndrew.getUsername() + "&" + param2 + "=" + "doesntexist";
		}

		MvcResult result1, result2;
		switch (requestType) {
			case "GET":
				result1 = mockMvc.perform(get(requestUrl1)).andExpect(status().isNotFound()).andReturn();
				result2 = mockMvc.perform(get(requestUrl2)).andExpect(status().isNotFound()).andReturn();
				break;
			case "POST":
				result1 = mockMvc.perform(post(requestUrl1)).andExpect(status().isNotFound()).andReturn();
				result2 = mockMvc.perform(post(requestUrl2)).andExpect(status().isNotFound()).andReturn();
				break;
			case "PUT":
				result1 = mockMvc.perform(put(requestUrl1)).andExpect(status().isNotFound()).andReturn();
				result2 = mockMvc.perform(put(requestUrl2)).andExpect(status().isNotFound()).andReturn();
				break;
			case "DELETE":
				result1 = mockMvc.perform(delete(requestUrl1)).andExpect(status().isNotFound()).andReturn();
				result2 = mockMvc.perform(delete(requestUrl2)).andExpect(status().isNotFound()).andReturn();
				break;
			default:
				result1 = mockMvc.perform(get(requestUrl1)).andExpect(status().isNotFound()).andReturn();
				result2 = mockMvc.perform(get(requestUrl2)).andExpect(status().isNotFound()).andReturn();
				break;
		}

		ResponsePayload response1 = getResponsePayloadFromMvcResult(result1);
		ResponsePayload response2 = getResponsePayloadFromMvcResult(result2);
		assertEquals(404, response1.getStatus());
		assertEquals(404, response2.getStatus());
		assertEquals("User with username " + "doesntexist" + " not found", response1.getMessage());
		assertEquals("User with username " + "doesntexist" + " not found", response2.getMessage());
	}

	@Test
	void sendFriendRequest_sentToSelf() throws Exception {
		MvcResult result = mockMvc.perform(
				post("/api/friend/sendRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userAndrew.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot send a friend request to oneself (" + userAndrew.getUsername() + ")",
				response.getMessage());
	}

	@Test
	void sendFriendRequest_alreadyFriends() throws Exception {
		addFriendship(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				post("/api/friend/sendRequest?sender=" + userBobby.getUsername() + "&receiver="
						+ userAndrew.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot send a friend request when already friends (from " + userBobby.getUsername() + " to "
				+ userAndrew.getUsername() + ")", response.getMessage());
	}

	@Test
	void sendFriendRequest_alreadyReceivingOne() throws Exception {
		addFriendRequest(userBobby, userAndrew);

		MvcResult result = mockMvc.perform(
				post("/api/friend/sendRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot send a friend request when having one already received (from " + userBobby.getUsername()
				+ " to " + userAndrew.getUsername() + ")", response.getMessage());
	}

	@Test
	void sendFriendRequest_alreadySentOne() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				post("/api/friend/sendRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot send a friend request twice (from " + userAndrew.getUsername() + " to "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void acceptFriendRequest_acceptingOnesOwnRequest() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				put("/api/friend/acceptRequest?sender=" + userBobby.getUsername() + "&receiver="
						+ userAndrew.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot accept one's own friend request (from " + userAndrew.getUsername() + " to "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void acceptFriendRequest_friendRequestDoesntExist() throws Exception {
		MvcResult result = mockMvc.perform(
				put("/api/friend/acceptRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot accept friend request when no friend request found (from " + userAndrew.getUsername()
				+ " to " + userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void cancelFriendRequest_cancelingRequestToSelf() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				delete("/api/friend/cancelRequest?sender=" + userBobby.getUsername() + "&receiver="
						+ userAndrew.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals(
				"Cannot cancel someone else's friend request (from " + userAndrew.getUsername() + " to "
						+ userBobby.getUsername() + ")",
				response.getMessage());
	}

	@Test
	void cancelFriendRequest_friendRequestDoesntExist() throws Exception {
		MvcResult result = mockMvc.perform(
				delete("/api/friend/cancelRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot cancel friend request when no friend request found (from " + userAndrew.getUsername()
				+ " to " + userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void declineFriendRequest_decliningOnesOwnRequest() throws Exception {
		addFriendRequest(userAndrew, userBobby);

		MvcResult result = mockMvc.perform(
				delete("/api/friend/declineRequest?sender=" + userBobby.getUsername() + "&receiver="
						+ userAndrew.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot decline one's own friend request (from " + userAndrew.getUsername() + " to "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void declineFriendRequest_friendRequestDoesntExist() throws Exception {
		MvcResult result = mockMvc.perform(
				delete("/api/friend/declineRequest?sender=" + userAndrew.getUsername() + "&receiver="
						+ userBobby.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot decline friend request when no friend request found (from " + userAndrew.getUsername()
				+ " to " + userBobby.getUsername() + ")", response.getMessage());
	}

	@Test
	void unfriend_friendshipDoesntExist() throws Exception {
		MvcResult result = mockMvc.perform(
				delete("/api/friend/unfriend?unfriender=" + userAndrew.getUsername() + "&unfriended="
						+ userBobby.getUsername()))
				.andExpect(status().isBadRequest())
				.andReturn();

		ResponsePayload response = getResponsePayloadFromMvcResult(result);

		assertEquals(400, response.getStatus());
		assertEquals("Cannot unfriend when no friendship found (between " + userAndrew.getUsername() + " and "
				+ userBobby.getUsername() + ")", response.getMessage());
	}

	// ---------------------------------------------------------------

	private void insertUserIntoDB(User user) {
		if (user != null) {
			userRepository.save(user);
		}
	}

	private void addFriendRequest(User sender, User receiver) {
		friendRequestsRepository.save(Objects.requireNonNull(
				FriendRequests.builder()
						.sender(sender)
						.receiver(receiver)
						.build()));
	}

	private void addFriendship(User user1, User user2) {
		friendshipsRepository.save(Objects.requireNonNull(
				Friendships.builder()
						.user1(user1)
						.user2(user2)
						.build()));
	}

	private void cleanDB() {
		friendRequestsRepository.deleteAll();
		friendshipsRepository.deleteAll();
		notificationRepository.deleteAll();
		userRepository.deleteAll();
	}

	ResponsePayload getResponsePayloadFromMvcResult(MvcResult result) throws UnsupportedEncodingException {
		var responseJson = JsonPath.parse(result.getResponse().getContentAsString());
		String message = responseJson.read("$.message");
		int status = responseJson.read("$.status");
		return ResponsePayload.builder()
				.message(message)
				.status(status)
				.build();
	}
}