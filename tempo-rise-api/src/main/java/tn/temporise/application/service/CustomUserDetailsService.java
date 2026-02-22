package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.EmailInterface;
import tn.temporise.domain.port.UserRepo;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepo userRepo;
  private final AuthRepo authRepo;
  private final ExceptionFactory exceptionFactory;
  private final EmailInterface emailInterface;

  @Override
  public CustomUserDetails loadUserByUsername(String username) {
    try {
      String[] parts = username.split(";");
      if (parts.length != 2) {
        log.info("Invalid input: {}", username);
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      String email = parts[0];
      log.info("email in parts[0]: {}", email);
      String providerId = parts[1];
      log.info("providerId in parts[1]: {}", providerId);
      UtilisateurModel user = userRepo.findByEmail(email);
      log.info("User found: {}", user);
      Authentification auth = authRepo.findByUserEmailAndProviderId(email, providerId);
      log.info("Auth found: {}", auth);
      // Skip verification check for OAuth users (providerId != "0")
      if ("0".equals(providerId)) {
        if (!user.isverified()) {
          if (user.activationtokenexpiresat() == null) {
            emailInterface.sendVerificationEmail(email);
          } else if (user.activationtokenexpiresat().isBefore(LocalDateTime.now()))
            emailInterface.sendVerificationEmail(email);
          throw exceptionFactory.badRequest("unauthorized.account_disabled");
        }
      }
      CustomUserDetails customUserDetails = CustomUserDetails.builder().id(user.id())
          .email(user.email()).username(user.prenom() + " " + user.nom()).nom(user.nom())
          .prenom(user.prenom()).telephone(user.telephone()).password(auth.password())
          .authorities(mapRolesToAuthorities(user.roles())).providerId(auth.providerId())
          .isverified(user.isverified()).isEnabled(true).isAccountNonExpired(true)
          .isAccountNonLocked(true).isCredentialsNonExpired(true).build();
      log.info("User details: {}", customUserDetails);
      return customUserDetails;
    } catch (NotFoundException | BadRequestException | UnauthorizedException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to load user details", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private Collection<GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
    return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toList());
  }
}
