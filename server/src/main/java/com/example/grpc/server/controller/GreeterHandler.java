package com.example.grpc.server.controller;

import com.example.grpc.common.model.*;
import com.example.grpc.common.service.UserServiceGrpc;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
class GreeterHandler extends UserServiceGrpc.UserServiceImplBase {

    private Map<String, String> users = new LinkedHashMap<>() {{
        put("admin", "admin");
        put("user1", "pw1");
        put("user2", "pw2");
    }};

    @Override
    public void testApp(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        responseObserver.onNext(MessageResponse.newBuilder().setMessage("receive: " + request.getMessage()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
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
        int num = Math.min(request.getNumberUser(), users.size());
        ArrayList<String> username = new ArrayList<>(users.keySet());
        ArrayList<String> password = new ArrayList<>(users.values());

        for (int i = 0; i < num; i++) {
            User user = User.newBuilder().setUsername(username.get(i)).setPassword(password.get(i)).build();
            responseObserver.onNext(user);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<User> addUsers(StreamObserver<MessageResponse> responseObserver) {
        return new StreamObserver<User>() {
            User user;
            int count = 0;

            @Override
            public void onNext(User value) {
                user = value;
                users.put(user.getUsername(), user.getPassword());
                count++;
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                MessageResponse response = MessageResponse.newBuilder().setMessage(count + " users are added").build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}
