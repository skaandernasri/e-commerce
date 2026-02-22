package tn.temporise.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tn.temporise.domain.model.CustomUserDetails;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<UtilisateurEntity> {

  @Override
  public Optional<UtilisateurEntity> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof CustomUserDetails userDetails) {
      return Optional.of(new UtilisateurEntity(userDetails.id()));
    }

    if (principal instanceof String username) {
      UtilisateurEntity utilisateur = new UtilisateurEntity();
      utilisateur.setEmail(username);
      return Optional.of(utilisateur);
    }

    return Optional.empty();
  }


}

