package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.mapper.AuthMapper;
import tn.temporise.application.service.CustomOAuth2UserService;
import tn.temporise.application.service.UploadImageService;
import tn.temporise.domain.model.Authentification;
import tn.temporise.domain.model.Role;
import tn.temporise.domain.model.TypeAuthentification;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.PanierRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAndAuthServiceTest {

  @Mock
  private UserRepo userRepo;
  @Mock
  private JwtUtil jwtUtil;
  @Mock
  private AuthRepo authRepo;
  @Mock
  private AuthMapper authMapper;
  @Mock
  private PanierRepo panierRepo;
  @Mock
  private UploadImageService uploadImageService;
  @InjectMocks
  private CustomOAuth2UserService customOAuth2UserService;
  private UtilisateurModel user;
  private Authentification auth;

  @BeforeEach
  void setUp() {
    String email = "test@example.com";
    String nom = "Test";
    String prenom = "User";
    String providerId = "provider1";
    user = UtilisateurModel.builder().email(email).nom(nom).prenom(prenom)
        .roles(Set.of(Role.CLIENT)).build();
    auth = Authentification.builder().user(user).providerId(providerId)
        .type(TypeAuthentification.EMAIL).token("token").build();
  }


  @Test
  void findOrCreateUser_ExistingUser_UpdatesAuth() throws Exception {
    Authentification existingAuth = auth.toBuilder().token("old-token").build();

    when(userRepo.findByEmail(anyString())).thenReturn(user);
    when(authRepo.findByUserEmailAndProviderId(anyString(), anyString())).thenReturn(existingAuth);
    when(authRepo.save(any(Authentification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    AuthentificationEntity entity = new AuthentificationEntity();
    entity.setRefreshToken("new-token");
    when(authMapper.modelToEntity(any(Authentification.class))).thenReturn(entity);
    when(authMapper.entityToModel(any(AuthentificationEntity.class)))
        .thenReturn(existingAuth.toBuilder().token("new-token").providerId("0").user(user).build());

    customOAuth2UserService.findOrCreateUser(user);

    verify(userRepo, times(1)).findByEmail(anyString());
    verify(authRepo, times(1)).findByUserEmailAndProviderId(anyString(), anyString());
  }

  @Test
  void findOrCreateAuth_ExistingAuth_UpdatesToken() throws Exception {
    UtilisateurModel user =
        UtilisateurModel.builder().email("test@example.com").roles(Set.of(Role.CLIENT)).build();

    Authentification existingAuth = Authentification.builder().id(1L).user(user)
        .type(TypeAuthentification.EMAIL).providerId("0").token("old-token").build();

    when(authRepo.findByUserEmailAndProviderId(anyString(), anyString())).thenReturn(existingAuth);

    AuthentificationEntity entity = new AuthentificationEntity();
    entity.setRefreshToken("new-token");
    when(authMapper.modelToEntity(any(Authentification.class))).thenReturn(entity);
    when(authMapper.entityToModel(any(AuthentificationEntity.class)))
        .thenReturn(existingAuth.toBuilder().token("new-token").build());

    when(authRepo.save(any(Authentification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Authentification result = customOAuth2UserService.findOrCreateAuth(user);

    assertNotNull(result);
    assertEquals("new-token", result.token());
  }
}
