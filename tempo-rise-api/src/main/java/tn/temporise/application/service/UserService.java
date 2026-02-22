package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.UserMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.UserRepo;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepo userRepo;
  private final ExceptionFactory exceptionFactory;
  private final UserMapper userMapper;
  private final AuthRepo authRepo;
  private final PasswordEncoder passwordEncoder;
  private final UploadImageService uploadImageService;

  public UserResponse createUser(UserRequest userRequest) {
    try {
      // Validate input
      // registrationService.validatePassword(userRequest.getPassword());
      if (userRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }


      // Check if email already exists
      try {
        UtilisateurModel existingUser = userRepo.findByEmail(userRequest.getEmail());
        if (existingUser != null) {
          throw exceptionFactory.conflict("conflict.email_exists");
        }
      } catch (NotFoundException ignored) {
        // Email not found - this is good, we can proceed
      }

      // Map and save new user
      UtilisateurModel utilisateurModel = userMapper.toModel(userRequest);
      UtilisateurModel savedUser = userRepo.save(utilisateurModel);
      Authentification authentification = Authentification.builder().user(savedUser).providerId("0")
          // .password(passwordEncoder.encode(userRequest.getPassword()))
          .type(TypeAuthentification.EMAIL).token(null).build();
      authRepo.save(authentification);

      return userMapper.modelToResponse(savedUser);

    } catch (NullPointerException | BadRequestException | ConflictException e) {
      log.warn("User creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public UserResponse getUserById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      UtilisateurModel user = userRepo.findById(id);
      return userMapper.modelToResponse(user);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<UserResponse> getAllUsers() {
    try {
      // Assuming you have a findAll method in your repo that returns List<UtilisateurModel>
      return userRepo.findAll().stream().map(userMapper::modelToResponse)
          .collect(Collectors.toList());

    } catch (NotFoundException e) {
      log.warn("No users found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public UserResponse updateUser(Long id, UserRequest userRequest) {
    try {
      if (id == null || userRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      UtilisateurModel existingUser = userRepo.findById(id);

      // Check if the email is being changed
      if (!existingUser.email().equals(userRequest.getEmail())) {
        try {
          UtilisateurModel existingUserByEmail = userRepo.findByEmail(userRequest.getEmail());

          // If we find a user and it's not the current one -> conflict
          if (!existingUserByEmail.id().equals(id)) {
            throw exceptionFactory.conflict("conflict.email_exists");
          }

        } catch (ConflictException e) {
          throw e;
        } catch (NotFoundException ignored) {
          // Safe to continue – no user exists with the new email
        }
      }

      userRequest.setId(id);
      UtilisateurModel updatedUser = userMapper.toModel(userRequest);
      log.info("Updated user: {}", updatedUser);
      UtilisateurModel savedUser = userRepo.update(updatedUser);
      log.info("Saved user: {}", savedUser);
      try {
        authRepo.findByUserEmailAndProviderId(savedUser.email(), "0");
        log.info("Authentification found for user {}", savedUser.email());
      } catch (NotFoundException e) {
        log.info("Authentification not found for user {}", savedUser.email());
        Authentification authentification = Authentification.builder().user(savedUser)
            .providerId("0").password(passwordEncoder.encode(UUID.randomUUID() + ""))
            .type(TypeAuthentification.EMAIL).token(null).build();
        authRepo.save(authentification);
      }

      return userMapper.modelToResponse(savedUser);

    } catch (NotFoundException e) {
      throw exceptionFactory.notFound("notfound.user");
    }
  }

  public void deleteUser(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      UtilisateurModel user = userRepo.findById(id);
      if (user != null && user.imageUrl() != null) {
        uploadImageService.deleteImage(user.imageUrl());
      }
      // Assuming you have a deleteById method in your repo
      userRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteAllUsers() {
    try {
      userRepo.findAll();
      userRepo.deleteAll();

    } catch (NotFoundException e) {
      log.warn("No users to delete: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  // Additional methods specific to User operations
  public UserResponse findByEmail(String email) {
    try {
      if (email == null || email.isBlank()) {
        throw exceptionFactory.badRequest("badrequest.invalid_email");
      }

      UtilisateurModel user = userRepo.findByEmail(email);
      return userMapper.modelToResponse(user);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User retrieval by email failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User retrieval by email failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Set<Role> findUserRoles(Long userId) {
    try {
      if (userId == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      return userRepo.findRolesById(userId);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User roles retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User roles retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteByEmail(String email) {
    try {
      if (email == null || email.isBlank()) {
        throw exceptionFactory.badRequest("badrequest.invalid_email");
      }

      userRepo.deleteByEmail(email);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User deletion by email failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User deletion by email failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public UserResponse findByActivationToken(String token) {
    try {
      if (token == null || token.isBlank()) {
        throw exceptionFactory.badRequest("badrequest.invalid_token");
      }

      UtilisateurModel user = userRepo.findByActivationToken(token);
      return userMapper.modelToResponse(user);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User retrieval by activation token failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User retrieval by activation token failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public UserResponse findByResetPasswordToken(String token) {
    try {
      if (token == null || token.isBlank()) {
        throw exceptionFactory.badRequest("badrequest.invalid_token");
      }

      UtilisateurModel user = userRepo.findByResetPasswordToken(token);
      return userMapper.modelToResponse(user);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User retrieval by reset token failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User retrieval by reset token failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public UserResponse updateProfile(UserRequest userRequest) {
    try {
      if (userRequest == null || userRequest.getId() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      UtilisateurModel user = userRepo.findById(userRequest.getId());
      UtilisateurModel request = userMapper.toModel(userRequest);
      log.info("User profile update request: {}", request);
      UtilisateurModel updatedUser = user.toBuilder().nom(request.nom())
          .date_naissance(request.date_naissance()).genre(request.genre()).prenom(request.prenom())
          .telephone(request.telephone()).build();
      log.info("Updated user: {}", updatedUser);
      UtilisateurModel savedUser = userRepo.save(updatedUser);
      return userMapper.modelToResponse(savedUser);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User profile update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User profile update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public UpdateImageProfilUtilisateur200Response uploadProfilePicture(Long userId,
      MultipartFile image) {
    try {
      if (userId == null || image == null || image.isEmpty()) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      UtilisateurModel user = userRepo.findById(userId);
      ImageUploadResponse imageUploadResponse = uploadImageService.uploadImage(image);
      UtilisateurModel updatedUser =
          user.toBuilder().imageUrl(imageUploadResponse.getFilename()).build();
      userRepo.save(updatedUser);
      if (user != null && user.imageUrl() != null) {
        uploadImageService.deleteImage(user.imageUrl());
      }
      UpdateImageProfilUtilisateur200Response updateImageProfilUtilisateur200Response =
          new UpdateImageProfilUtilisateur200Response();
      updateImageProfilUtilisateur200Response.setImageUrl(imageUploadResponse.getFilename());

      return updateImageProfilUtilisateur200Response;
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image upload failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image upload failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Resource getImageProfilUtilisateur(Long userId) throws Exception {
    try {
      if (userId == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      UtilisateurModel user = userRepo.findById(userId);

      byte[] imageBytes = user.image();
      if (imageBytes == null) {
        throw exceptionFactory.notFound("notfound.image");
      }

      return new ByteArrayResource(imageBytes);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

}
