package tn.temporise.tu;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.UserMapper;
import tn.temporise.application.service.RegistrationService;
import tn.temporise.application.service.UploadImageService;
import tn.temporise.application.service.UserService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.UserRepo;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Slf4j
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepo userRepo;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private UserMapper userMapper;

  @Mock
  private AuthRepo authRepo;

  @Mock
  private UploadImageService uploadImageService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MultipartFile image;
  @Mock
  private RegistrationService registrationService;

  private String imagePath;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    image = new MockMultipartFile("image", new byte[] {4, 5, 6});
    imagePath = "imagePath";
    when(exceptionFactory.badRequest(anyString())).thenThrow(new BadRequestException("", ""));
    when(exceptionFactory.conflict(anyString())).thenThrow(new ConflictException("", ""));
    when(exceptionFactory.notFound(anyString())).thenThrow(new NotFoundException("", ""));;
  }

  @Test
  void shouldCreateUserSuccessfully() {
    UserRequest request = new UserRequest();
    request.setEmail("test@example.com");

    UtilisateurModel model = UtilisateurModel.builder().id(1L).email(request.getEmail())
        .roles(Set.of(Role.CLIENT)).nom("Test User").password(null).isverified(false).build();
    when(userRepo.findByEmail(request.getEmail()))
        .thenThrow(new NotFoundException("notfound.user", ""));
    when(userMapper.toModel(request)).thenReturn(model);
    when(userRepo.save(model)).thenReturn(model);
    UserResponse response1 = new UserResponse();
    response1.setEmail("test@example.com");
    response1.setId(1L);
    when(userMapper.modelToResponse(model)).thenReturn(response1);

    UserResponse response = userService.createUser(request);

    assertThat(response.getEmail()).isEqualTo("test@example.com");
    verify(authRepo).save(any(Authentification.class));
  }

  @Test
  void shouldThrowConflictWhenEmailExists() {
    UserRequest request = new UserRequest();
    request.setEmail("exists@example.com");
    UtilisateurModel existing = UtilisateurModel.builder().id(2L).email(request.getEmail())
        .roles(Set.of(Role.CLIENT)).nom("Test User").password(null).isverified(false).build();
    when(userRepo.findByEmail(request.getEmail())).thenReturn(existing);

    assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(ConflictException.class);
  }

  @Test
  void shouldGetUserById() {
    UtilisateurModel model = UtilisateurModel.builder().id(1L).email("user@example.com")
        .roles(Set.of(Role.CLIENT)).nom("Test User").password(null).isverified(false).build();
    when(userRepo.findById(1L)).thenReturn(model);
    UserResponse response1 = new UserResponse();
    response1.setEmail("test@example.com");
    response1.setId(1L);
    when(userMapper.modelToResponse(model)).thenReturn(response1);

    UserResponse response = userService.getUserById(1L);

    assertThat(response.getEmail()).isEqualTo("test@example.com");
  }

  @Test
  void shouldUpdateUserSuccessfully() {
    Long id = 1L;
    UserRequest request = new UserRequest();
    request.setEmail("new@example.com");
    UtilisateurModel existingUser = UtilisateurModel.builder().id(id).email("old@example.com")
        .roles(Set.of(Role.CLIENT)).nom("Test User").password(null).isverified(false).build();
    UtilisateurModel updated = UtilisateurModel.builder().id(id).email("new@example.com")
        .roles(Set.of(Role.GESTIONNAIRE)).nom("Test User").password(null).isverified(false).build();
    when(userRepo.findById(id)).thenReturn(existingUser);
    when(userRepo.findByEmail("new@example.com"))
        .thenThrow(new NotFoundException("notfound.user", ""));
    when(userMapper.toModel(request)).thenReturn(updated);
    when(userRepo.update(updated)).thenReturn(updated);
    UserResponse response1 = new UserResponse();
    response1.setEmail("new@example.com");
    response1.setId(id);
    when(userMapper.modelToResponse(updated)).thenReturn(response1);

    UserResponse result = userService.updateUser(id, request);
    assertThat(result.getEmail()).isEqualTo("new@example.com");
  }

  @Test
  void shouldUpdateUserProfileSuccessfully() {
    UserRequest request = new UserRequest();
    request.setId(1L);
    request.setNom("Updated Nom");
    request.setPrenom("Updated Prenom");
    request.setTelephone("5555555555");
    UtilisateurModel existingUser = UtilisateurModel.builder().id(1L).nom("Test User")
        .prenom("Test Prenom").telephone("1234567890").build();
    UtilisateurModel updated = UtilisateurModel.builder().id(1L).nom("Updated Nom")
        .prenom("Updated Prenom").telephone("5555555555").build();
    when(userRepo.findById(1L)).thenReturn(existingUser);
    when(userMapper.toModel(request)).thenReturn(updated);
    when(userRepo.save(updated)).thenReturn(updated);
    UserResponse response1 = new UserResponse();
    response1.setNom("Updated Nom");
    response1.setPrenom("Updated Prenom");
    response1.setTelephone("5555555555");
    response1.setId(1L);
    when(userMapper.modelToResponse(updated)).thenReturn(response1);

    UserResponse result = userService.updateProfile(request);
    assertThat(result.getNom()).isEqualTo("Updated Nom");
    assertThat(result.getPrenom()).isEqualTo("Updated Prenom");
    assertThat(result.getTelephone()).isEqualTo("5555555555");

  }

  @Test
  void shouldUploadProfilePictureSuccessfully() throws IOException {
    Long userId = 1L;
    UtilisateurModel existingUser = UtilisateurModel.builder().id(userId).build();
    ImageUploadResponse imageUploadResponse = new ImageUploadResponse();
    imageUploadResponse.setFilename("test.jpg");
    // when(image.getBytes()).thenReturn(new byte[]{4, 5, 6});
    UtilisateurModel updated = existingUser.toBuilder().imageUrl(imagePath).build();
    when(uploadImageService.uploadImage(any(MultipartFile.class))).thenReturn(imageUploadResponse);
    when(userRepo.findById(userId)).thenReturn(existingUser);
    when(userRepo.save(updated)).thenReturn(updated);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Profile picture uploaded successfully");
    UpdateImageProfilUtilisateur200Response result =
        userService.uploadProfilePicture(userId, image);
    assertNotNull(result.getImageUrl());
  }

  @Test
    void shouldThrowNotFoundOnUpdate() {
        when(userRepo.findById(99L)).thenThrow(new NotFoundException("notfound.user", ""));

        assertThatThrownBy(() -> userService.updateUser(99L, new UserRequest()))
                .isInstanceOf(NotFoundException.class);
    }

  @Test
  void shouldDeleteUserSuccessfully() {
    Long id = 1L;
    when(userRepo.findById(id)).thenReturn(any(UtilisateurModel.class));

    userService.deleteUser(id);

    verify(userRepo).deleteById(id);
  }
}
