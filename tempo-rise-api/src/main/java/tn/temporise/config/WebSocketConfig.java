package tn.temporise.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import tn.temporise.infrastructure.security.utils.WebSocketHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  @Autowired
  private WebSocketHandshakeInterceptor interceptor;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", "/queue"); // public and private
    config.setUserDestinationPrefix("/user"); // for sending to specific users
    config.setApplicationDestinationPrefixes("/app"); // prefix from client
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").addInterceptors(interceptor)
        .setHandshakeHandler(new CustomHandshakeHandler())
        .setAllowedOrigins("https://temposphere.tn", "https://www.temposphere.tn",
            "http://localhost:4200")
        .withSockJS();
  }
}

