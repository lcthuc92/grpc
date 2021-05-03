package com.example.grpc.client;

import com.example.grpc.client.helper.BearerToken;
import com.example.grpc.common.Constants;
import com.example.grpc.common.service.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Value("${grpc.server.host}")
	private String host;

	@Value("${grpc.server.port}")
	private int port;

	@Bean
	public ManagedChannel managedChannel() {
		return ManagedChannelBuilder.forAddress(host, port)
			.usePlaintext()
			.build();
	}

	@Bean
	public static String getJwt() {
		return Jwts.builder()
			.setSubject("GreetingClient") // client's identifier
			.signWith(SignatureAlgorithm.HS256, Constants.JWT_SIGNING_KEY)
			.compact();
	}

	@Bean
	public UserServiceGrpc.UserServiceBlockingStub blockingStub(ManagedChannel channel) {
		return UserServiceGrpc.newBlockingStub(channel).withCallCredentials(new BearerToken(getJwt()));
	}

	@Bean
	public UserServiceGrpc.UserServiceStub userServiceStub(ManagedChannel channel) {
		return UserServiceGrpc.newStub(channel).withCallCredentials(new BearerToken(getJwt()));
	}
}
