package com.imageprocessor.security;

import com.imageprocessor.model.Plan;
import com.imageprocessor.model.Subscription;
import com.imageprocessor.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class JwtUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean emailVerified;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isPremium;
    private String planName;

    public static JwtUserDetails build(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Todos os usuários têm o papel básico
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Verificar se o usuário tem plano premium para adicionar autoridade correspondente
        boolean isPremium = false;
        String planName = "Free";

        Subscription subscription = user.getSubscription();
        if (subscription != null && subscription.isActive()) {
            Plan plan = subscription.getPlan();
            planName = plan.getName();

            if (plan.isPremium()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM"));
                isPremium = true;
            }
        }

        return JwtUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .emailVerified(user.isEmailVerified())
                .authorities(authorities)
                .isPremium(isPremium)
                .planName(planName)
                .build();
    }

    public static JwtUserDetails buildWithoutEmailVerification(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Todos os usuários têm o papel básico
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Verificar se o usuário tem plano premium para adicionar autoridade correspondente
        boolean isPremium = false;
        String planName = "Free";

        Subscription subscription = user.getSubscription();
        if (subscription != null && subscription.isActive()) {
            Plan plan = subscription.getPlan();
            planName = plan.getName();

            if (plan.isPremium()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_PREMIUM"));
                isPremium = true;
            }
        }

        return JwtUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .emailVerified(true)  // Sempre definir como true para ignorar essa verificação
                .authorities(authorities)
                .isPremium(isPremium)
                .planName(planName)
                .build();
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
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Para desenvolvimento, sempre retorna true
        return true;
    }
}