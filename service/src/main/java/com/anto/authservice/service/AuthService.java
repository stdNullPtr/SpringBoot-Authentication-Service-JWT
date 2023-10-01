package com.anto.authservice.service;

import com.anto.authservice.exception.ApiException;
import com.anto.authservice.exception.TokenRefreshException;
import com.anto.authservice.model.ERole;
import com.anto.authservice.model.RefreshToken;
import com.anto.authservice.model.Role;
import com.anto.authservice.model.User;
import com.anto.authservice.model.payload.request.LoginRequest;
import com.anto.authservice.model.payload.request.SignupRequest;
import com.anto.authservice.model.payload.request.TokenRefreshRequest;
import com.anto.authservice.model.payload.response.JwtResponse;
import com.anto.authservice.model.payload.response.MessageResponse;
import com.anto.authservice.model.payload.response.TokenRefreshResponse;
import com.anto.authservice.repository.RoleRepository;
import com.anto.authservice.repository.UserRepository;
import com.anto.authservice.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public JwtResponse signin(@Valid LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwt(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        refreshTokenService.deleteByUserId(userDetails.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new JwtResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    public MessageResponse signup(@Valid SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new ApiException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ApiException("Error: Email is already in use!");
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        // @TODO: enforce only user roles created through api after development is done
        Set<String> requestRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (requestRoles == null) {
            Role userRole = getRoleFromDb(ERole.ROLE_USER.getRoleString());
            roles.add(userRole);
        } else {
            requestRoles.forEach(role -> roles.add(getRoleFromDb(role)));
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }

    private Role getRoleFromDb(String role) {
        ERole eRole = ERole.fromString(role);
        switch (eRole) {
            case ROLE_ADMIN -> {
                return roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new ApiException("Error: Role is not found."));
            }
            case ROLE_MODERATOR -> {
                return roleRepository.findByName(ERole.ROLE_MODERATOR)
                        .orElseThrow(() -> new ApiException("Error: Role is not found."));
            }
            case ROLE_USER -> {
                return roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new ApiException("Error: Role is not found."));
            }
            default -> throw new ApiException("Error: Role is not found.");
        }
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return new TokenRefreshResponse(token, requestRefreshToken);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }
}
