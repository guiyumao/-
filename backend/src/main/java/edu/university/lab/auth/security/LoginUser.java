package edu.university.lab.auth.security;

import edu.university.lab.module.user.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 登录用户主体
 */
@Getter
public class LoginUser implements UserDetails {

    private final User user;

    private final List<String> roleCodes;

    private final String activeRoleCode;

    private final List<String> permissionCodes;

    private final List<SimpleGrantedAuthority> authorities;

    public LoginUser(User user, List<String> roleCodes, String activeRoleCode, List<String> permissionCodes) {
        this.user = user;
        this.roleCodes = roleCodes;
        this.activeRoleCode = activeRoleCode;
        this.permissionCodes = permissionCodes;
        this.authorities = java.util.stream.Stream.concat(
                (activeRoleCode == null ? roleCodes : List.of(activeRoleCode))
                    .stream()
                    .map(code -> new SimpleGrantedAuthority("ROLE_" + code.toUpperCase())),
                permissionCodes.stream().map(SimpleGrantedAuthority::new)
            )
            .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
