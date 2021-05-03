package com.example.grpc.client;

import com.example.grpc.common.service.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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

   /* @Bean
    public ManagedChannel managedChannelSSL() throws SSLException {
        // With server authentication SSL/TLS; custom CA root certificates; not on Android
        return NettyChannelBuilder.forAddress(host, port)
            .sslContext(GrpcSslContexts.forClient().trustManager(new File("roots.pem")).build())
            .build();
    }*/

	@Bean
	public UserServiceGrpc.UserServiceBlockingStub blockingStub(ManagedChannel channel) {
		return UserServiceGrpc.newBlockingStub(channel);
	}

	@Bean
	public UserServiceGrpc.UserServiceStub userServiceStub(ManagedChannel channel) {
		return UserServiceGrpc.newStub(channel);
	}
}
