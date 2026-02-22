package tn.temporise.infrastructure.security.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import tn.temporise.application.service.CustomUserDetailsService;
import tn.temporise.domain.model.CustomUserDetails;

import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

  @Autowired
  private JwtUtil jwtUtil; // your service for parsing JWT

  @Autowired
  private CustomUserDetailsService userDetailsService;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
    if (request instanceof ServletServerHttpRequest servletRequest) {

      String jwt = jwtUtil.getJwtFromCookies(servletRequest.getServletRequest());
      if (jwt != null) {
        String email = jwtUtil.extractEmail(jwt);
        String providerId = jwtUtil.extractProviderId(jwt);
        CustomUserDetails userDetails =
            userDetailsService.loadUserByUsername(email + ";" + providerId);
        jwtUtil.validateToken(jwt, userDetails);
        attributes.put("email", email);
        return true;
      }
    }
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Exception exception) {

  }
}
