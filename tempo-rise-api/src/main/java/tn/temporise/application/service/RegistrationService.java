package tn.temporise.application.service;


import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.RegMapper;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;

import java.io.IOException;


@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {
  private final UserRepo userRepo;
  private final AuthRepo authRepo;
  private final PasswordEncoder passwordEncoder;
  private final RegMapper regMapper;
  private final ExceptionFactory exceptionFactory;
  private final EmailInterface emailInterface;
  private final PanierRepo panierRepo;
  private final WhishlistRepo whishlistRepo;
  @Value("${password.pattern}")
  private String passwordPattern;

  @Transactional
  public void register(SignupUserRequest user)
      throws MessagingException, TemplateException, IOException {
    validatePassword(user.getPassword());

    try {
      // Check if user exists
      UtilisateurModel existingUser = userRepo.findByEmail(user.getEmail());

      // User exists - check authentication methods
      try {
        // Check if email/password auth exists
        authRepo.findByUserEmailAndProviderId(user.getEmail(), "0");
        throw exceptionFactory.conflict("conflict.email_exists");

      } catch (NotFoundException ignore) {
        emailInterface.sendVerificationEmail(existingUser.email());
        Authentification authModel = new Authentification(existingUser,
            passwordEncoder.encode(user.getPassword()), TypeAuthentification.EMAIL, "0" // Provider
                                                                                        // ID for
                                                                                        // email/password
        );

        authRepo.save(authModel);
      } catch (ConflictException e) {
        throw e;
      }

    } catch (NotFoundException e) {
      // User doesn't exist - proceed with registration
      registerNewUser(user);
    } catch (ConflictException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private void registerNewUser(SignupUserRequest user)
      throws MessagingException, IOException, TemplateException {
    UtilisateurModel userModel = regMapper.toModel(user);

    log.info("Signing up user request: {}", user);
    userModel = userRepo.save(userModel);
    Authentification authModel = new Authentification(userModel,
        passwordEncoder.encode(user.getPassword()), TypeAuthentification.EMAIL, "0" // Provider ID
    // for
    // email/password
    );

    authRepo.save(authModel);
    panierRepo.save(Panier.builder().utilisateur(userModel).build());
    whishlistRepo.save(Whishlist.builder().utilisateur(userModel).build());
    emailInterface.sendVerificationEmail(userModel.email());


    log.info("User registered successfully: {}", user.getEmail());

  }


  public void validatePassword(String password) {
    if (password == null || password.isEmpty()) {
      throw exceptionFactory.badRequest("badrequest.password.empty");
    }
    if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
      throw exceptionFactory.badRequest("badrequest.password.weak");
    }
  }
}
