package tn.temporise.application.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.exception.client.PasswordException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.UserRepo;


import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordService {
  private final RegistrationService registrationService;
  private final UserRepo userRepo;
  private final ExceptionFactory exceptionFactory;
  private final AuthRepo authRepo;
  private final PasswordEncoder passwordEncoder;


  public void validateResetToken(String token) {
    try {
      UtilisateurModel user = userRepo.findByResetPasswordToken(token);
      if (isTokenExpired(user.resetpasswordexpiresat()))
        throw exceptionFactory.unauthorized("unauthorized.token_expired");
      log.info("user after mapping to activate Account {}", user);
    } catch (NotFoundException | ConflictException | UnauthorizedException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public void resetPassword(String token, PasswordResetTokenRequest passwordResetTokenRequest) {
    try {
      registrationService.validatePassword(passwordResetTokenRequest.getNewPassword());
      if (!passwordResetTokenRequest.getNewPassword()
          .equals(passwordResetTokenRequest.getConfirmPassword())) {
        throw exceptionFactory.badRequest("badrequest.password.mismatch");
      }
      log.info("token got in resetPassword from cookie {}", token);
      UtilisateurModel user = userRepo.findByResetPasswordToken(token);
      UtilisateurModel updateUser =
          user.toBuilder().resetpasswordexpiresat(null).resetpasswordtoken(null).build();
      // UtilisateurModel updateUser = UtilisateurModel.builder().id(user.id()).email(user.email())
      // .nom(user.nom()).roles(user.roles()).isverified(user.isverified())
      // .activation_token(user.activation_token())
      // .activationtokenexpiresat(user.activationtokenexpiresat()).resetpasswordtoken(null)
      // .resetpasswordexpiresat(null).build();
      try {
        Authentification auth = authRepo.findByUserEmailAndProviderId(user.email(), "0");
        if (passwordEncoder.matches(passwordResetTokenRequest.getNewPassword(), auth.password()))
          throw exceptionFactory.conflict("conflict.same_password");
        Authentification updatedAuth = auth.toBuilder()
            .password(passwordEncoder.encode(passwordResetTokenRequest.getNewPassword())).build();
        authRepo.save(updatedAuth);
        log.info("updated Auth in resetPassword {}", updatedAuth);

      } catch (NotFoundException ignore) {
        Authentification newAuth = Authentification.builder().user(user)
            .password(passwordEncoder.encode(passwordResetTokenRequest.getNewPassword()))
            .type(TypeAuthentification.EMAIL).providerId("0").build();
        authRepo.save(newAuth);
        log.info("updated Auth in resetPassword {}", newAuth);
      }
      log.info("updated User in resetPassword {}", updateUser);
      userRepo.save(updateUser);
    } catch (NotFoundException | ConflictException | BadRequestException | PasswordException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public boolean isTokenExpired(LocalDateTime expirationDate) {
    LocalDateTime now = LocalDateTime.now();
    return now.isAfter(expirationDate); // Returns true if current time is after expiration time
  }

}
