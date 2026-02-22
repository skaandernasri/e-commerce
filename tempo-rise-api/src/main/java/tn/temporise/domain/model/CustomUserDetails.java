package tn.temporise.domain.model;


import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


@Builder

public record CustomUserDetails(
        Long id,
        String email,
        String password,
        String nom,
        String prenom,
        String telephone,
        String username,
        Collection<? extends GrantedAuthority> authorities,
        String providerId,
        boolean isAccountNonLocked,
        boolean isAccountNonExpired,
        boolean isEnabled,
        boolean isCredentialsNonExpired,
        boolean isverified
) implements UserDetails {




    // Getter for providerId
    public String getProviderId() {
        return providerId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
