package com.xaxo.authservice.service;

import com.xaxo.authservice.exception.ApiException;
import com.xaxo.authservice.model.ERole;
import com.xaxo.authservice.model.Role;
import com.xaxo.authservice.model.User;
import com.xaxo.authservice.model.payload.request.LoginRequest;
import com.xaxo.authservice.model.payload.request.SignupRequest;
import com.xaxo.authservice.model.payload.response.JwtResponse;
import com.xaxo.authservice.model.payload.response.MessageResponse;
import com.xaxo.authservice.repository.RoleRepository;
import com.xaxo.authservice.repository.UserRepository;
import com.xaxo.authservice.security.jwt.JwtUtils;
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

        return new JwtResponse(
                jwt,
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

        // Create new user's account
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
}
