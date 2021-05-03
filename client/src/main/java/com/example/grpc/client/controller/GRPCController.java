package com.example.grpc.client.controller;

import com.example.grpc.client.entity.RestLoginRequest;
import com.example.grpc.client.entity.RestUser;
import com.example.grpc.common.model.*;
import com.example.grpc.common.service.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GRPCController {
    private final static Logger logger = LoggerFactory.getLogger(GRPCController.class);

    @Autowired
    private ManagedChannel channel;

    @Autowired
    private UserServiceGrpc.UserServiceBlockingStub stub;

    @Autowired
    private UserServiceGrpc.UserServiceStub serviceStub;

    @GetMapping("/login")
    public String login(@RequestBody RestLoginRequest request) throws InterruptedException {
        try
        {
            User.Builder userBuilder = User.newBuilder().setUsername(request.getUsername()).setPassword(request.getPassword());
            LoginRequest loginRequest = LoginRequest.newBuilder().setUser(userBuilder).build();
            LoginResponse response = stub.login(loginRequest);
            return response.getMessage();
        } catch (RuntimeException ex) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            throw ex;
        }
    }

    @GetMapping("/users")
    public Object login(@RequestParam int numOfUsers) throws InterruptedException {
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        try
        {
            CollectUserRequest request = CollectUserRequest.newBuilder().setNumberUser(numOfUsers).build();
            Iterator<User> users = stub.getUsers(request);
            while (users.hasNext()) {
                User user = users.next();
                res.put(user.getUsername(), user.getPassword());
            }
            return res;
        } catch (RuntimeException ex) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            throw ex;
        }
    }

    @GetMapping("/addusers")
    public Object login(@RequestBody List<RestUser> users) throws InterruptedException {
        try
        {
            List<String> res = new ArrayList<>();
            StreamObserver<User> inputStreamObserver = serviceStub
                .addUsers(new StreamObserver<>() {
					@Override
					public void onNext(MessageResponse value) {
						res.add(value.getMessage());
					}

					@Override
					public void onError(Throwable t) {
					}

					@Override
					public void onCompleted() {
					}
				});

            for(RestUser restUser: users) {
                User user = User.newBuilder().setUsername(restUser.getUsername())
                    .setPassword(restUser.getPassword())
                    .build();
                inputStreamObserver.onNext(user);
            }
            inputStreamObserver.onCompleted();

            return res;
        } catch (RuntimeException ex) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            throw ex;
        }
    }
}
