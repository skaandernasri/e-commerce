package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.service.UserService;
import tn.temporise.domain.model.Response;
import tn.temporise.domain.model.UpdateImageProfilUtilisateur200Response;
import tn.temporise.domain.model.UserRequest;
import tn.temporise.domain.model.UserResponse;
import tn.temporise.infrastructure.api.UtilisateursApi;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController implements UtilisateursApi {
  private final UserService userService;

  @Override
  public ResponseEntity<UserResponse> _createUtilisateur(UserRequest userRequest) throws Exception {
    log.info("Creating new user with email: {}", userRequest.getEmail());
    UserResponse user = userService.createUser(userRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteUtilisateur(Long id) throws Exception {
    log.info("Deleting user with ID: {}", id);
    userService.deleteUser(id);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("User deleted successfully");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<List<UserResponse>> _getAllUtilisateurs() throws Exception {
    log.info("Fetching all users");
    List<UserResponse> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @Override
  public ResponseEntity<Resource> _getImageProfilUtilisateur(Long userId) throws Exception {
    return ResponseEntity.ok(userService.getImageProfilUtilisateur(userId));
  }

  @Override
  public ResponseEntity<UserResponse> _getUtilisateurById(Long id) throws Exception {
    log.info("Fetching user with ID: {}", id);
    UserResponse user = userService.getUserById(id);
    return ResponseEntity.ok(user);
  }

  @Override
  public ResponseEntity<UpdateImageProfilUtilisateur200Response> _updateImageProfilUtilisateur(
      Long userId, MultipartFile image) throws Exception {
    return ResponseEntity.ok(userService.uploadProfilePicture(userId, image));

  }

  @Override
  public ResponseEntity<UserResponse> _updateProfilUtilisateur(UserRequest userRequest)
      throws Exception {
    return ResponseEntity.ok(userService.updateProfile(userRequest));
  }

  @Override
  public ResponseEntity<UserResponse> _updateUtilisateur(Long id, UserRequest userRequest)
      throws Exception {
    log.info("Updating user with ID: {}", id);
    UserResponse updatedUser = userService.updateUser(id, userRequest);
    return ResponseEntity.ok(updatedUser);
  }

  // Additional endpoints that match your repository capabilities
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Response> _deleteAllUtilisateurs() throws Exception {
    log.info("Deleting all users");
    userService.deleteAllUsers();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("All users deleted successfully");
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<UserResponse> _getUtilisateurByEmail(String email) throws Exception {
    log.info("Fetching user by email: {}", email);
    UserResponse user = userService.findByEmail(email);
    return ResponseEntity.ok(user);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Response> _deleteUtilisateurByEmail(String email) throws Exception {
    log.info("Deleting user by email: {}", email);
    userService.deleteByEmail(email);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("User deleted successfully");
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<UserResponse> _getUtilisateurByActivationToken(String token)
      throws Exception {
    log.info("Fetching user by activation token");
    UserResponse user = userService.findByActivationToken(token);
    return ResponseEntity.ok(user);
  }

  public ResponseEntity<UserResponse> _getUtilisateurByResetPasswordToken(String token)
      throws Exception {
    log.info("Fetching user by reset password token");
    UserResponse user = userService.findByResetPasswordToken(token);
    return ResponseEntity.ok(user);
  }
}
