package com.example.grpc.server.controller;

import com.example.grpc.common.model.*;
import com.example.grpc.common.service.UserServiceGrpc;
import com.example.grpc.common.Constants;
import com.example.grpc.server.auth.AuthorizationServerInterceptor;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@GRpcService
class GreeterHandler extends UserServiceGrpc.UserServiceImplBase {

	private final static Logger logger = LoggerFactory.getLogger(GreeterHandler.class);

	private Map<String, String> users = new LinkedHashMap<>() {{
		put("admin", "admin");
		put("user1", "pw1");
		put("user2", "pw2");
		put("u1", "u1");
		put("u2", "u2");
		put("u3", "u3");
	}};

	@Override
	public void testApp(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
		String clientId = Constants.CLIENT_ID_CONTEXT_KEY.get();
		System.out.println("Processing request from " + clientId);

		responseObserver.onNext(MessageResponse.newBuilder().setMessage("receive: " + request.getMessage()).build());
		responseObserver.onCompleted();
	}

	@Override
	public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
		logger.info("login");
		String username = request.getUser().getUsername();
		String password = request.getUser().getPassword();
		boolean isSuccessful = users.containsKey(username) && users.containsValue(password);
		LoginResponse.Builder responseBuilder = LoginResponse.newBuilder()
			.setIsSuccessful(isSuccessful)
			.setMessage(isSuccessful ? "Login successfully!" : "Login failed!");
		responseObserver.onNext(responseBuilder.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getUsers(CollectUserRequest request, StreamObserver<User> responseObserver) {
		logger.info("get users");
		int num = Math.min(request.getNumberUser(), users.size());
		ArrayList<String> username = new ArrayList<>(users.keySet());
		ArrayList<String> password = new ArrayList<>(users.values());

		for (int i = 0; i < num; i++) {
			User user = User.newBuilder().setUsername(username.get(i)).setPassword(password.get(i)).build();
			logger.info((i + 1) + ": " + user);
			responseObserver.onNext(user);
		}

		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<User> addUsers(StreamObserver<MessageResponse> responseObserver) {
		return new StreamObserver<>() {
			int count = 0;

			@Override
			public void onNext(User value) {
				logger.info("add users: no." + count);
				if (!users.containsKey(value.getUsername())) {
					users.put(value.getUsername(), value.getPassword());
					count++;
				}
			}

			@Override
			public void onError(Throwable t) {
			}

			@Override
			public void onCompleted() {
				logger.info("add users: completed");
				MessageResponse response = MessageResponse.newBuilder().setMessage(count + " users are added").build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
			}
		};
	}

	@Override
	public StreamObserver<UpdatedUserRequest> updatedUsers(StreamObserver<User> responseObserver) {

		return new StreamObserver<>() {
			@Override
			public void onNext(UpdatedUserRequest updatedUserRequest) {
				logger.info("updating:" + updatedUserRequest);
				if (users.containsKey(updatedUserRequest.getUsername())) {
					users.put(updatedUserRequest.getUsername(), updatedUserRequest.getPassword());
					logger.info("Completed:" + updatedUserRequest);

					responseObserver.onNext(User.newBuilder().setUsername(updatedUserRequest.getUsername())
						.setPassword(updatedUserRequest.getPassword())
						.setIsUpdated(true).build());
				}
			}

			@Override
			public void onError(Throwable throwable) {
			}

			@Override
			public void onCompleted() {
				logger.info("update users: completed");
				responseObserver.onCompleted();
			}
		};
	}
}
