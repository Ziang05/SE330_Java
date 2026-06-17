package com.hospital.service.impl;

import com.hospital.audit.Auditable;
import com.hospital.dto.request.CreateUserRequest;
import com.hospital.dto.request.ResetPasswordRequest;
import com.hospital.dto.request.UpdateUserRequest;
import com.hospital.dto.response.UserResponse;
import com.hospital.entity.*;
import com.hospital.entity.enums.RoleName;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.RoleRepository;
import com.hospital.repository.UserRepository;
import com.hospital.repository.UserRoleRepository;
import com.hospital.security.UserPrincipal;
import com.hospital.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @Auditable(action = "CREATE", entityType = "User")
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User already exists with username: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        List<Role> roles = resolveRoles(request.getRoleNames());
        Doctor doctor = resolveDoctor(request.getDoctorId(), roles);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setIsActive(true);
        user.setDoctor(doctor);

        User saved = userRepository.save(user);
        saveUserRoles(saved, roles);
        return toResponse(saved, roles);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUser(id);
        return toResponse(user, findRolesByUserId(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return toResponse(user, findRolesByUserId(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String keyword, Boolean isActive, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return userRepository.findByKeywordAndStatus(normalizedKeyword, isActive, pageable)
                .map(user -> toResponse(user, findRolesByUserId(user.getId())));
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE", entityType = "User")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUser(id);

        if (request.getEmail() != null) {
            validateUniqueEmail(request.getEmail(), id);
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        List<Role> roles;
        if (request.getRoleNames() == null) {
            roles = findRolesByUserId(user.getId());
        } else {
            roles = resolveRoles(request.getRoleNames());
            userRoleRepository.deleteByUserId(user.getId());
            saveUserRoles(user, roles);
        }

        if (request.getDoctorId() != null) {
            user.setDoctor(resolveDoctor(request.getDoctorId(), roles));
        } else if (!hasRole(roles, RoleName.DOCTOR)) {
            user.setDoctor(null);
        }

        return toResponse(userRepository.save(user), roles);
    }

    @Override
    @Transactional
    @Auditable(action = "TOGGLE_ACTIVE", entityType = "User")
    public void toggleActive(Long id) {
        User user = findUser(id);
        if (Boolean.TRUE.equals(user.getIsActive()) && id.equals(getCurrentUserId())) {
            throw new BusinessException("Không thể vô hiệu hóa tài khoản của chính mình");
        }
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    @Auditable(action = "RESET_PASSWORD", entityType = "User")
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private void validateUniqueEmail(String email, Long currentUserId) {
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("User already exists with email: " + email);
                });
    }

    private List<Role> resolveRoles(List<String> roleNames) {
        Set<RoleName> uniqueRoleNames = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            uniqueRoleNames.add(parseRoleName(roleName));
        }

        return uniqueRoleNames.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new BusinessException("Role is not configured: " + roleName.name())))
                .sorted(Comparator.comparing(role -> role.getRoleName().name()))
                .toList();
    }

    private RoleName parseRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new BusinessException("Role name is invalid");
        }
        try {
            return RoleName.valueOf(roleName.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Role name is invalid: " + roleName);
        }
    }

    private Doctor resolveDoctor(Long doctorId, List<Role> roles) {
        if (doctorId == null) {
            return null;
        }
        if (!hasRole(roles, RoleName.DOCTOR)) {
            throw new BusinessException("doctorId chỉ được điền khi role là DOCTOR");
        }
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId));
    }

    private com.hospital.entity.Patient resolvePatient(Long patientId, List<Role> roles) {
        if (patientId == null) {
            return null;
        }
        if (!hasRole(roles, RoleName.PATIENT)) {
            throw new BusinessException("patientId chỉ được điền khi role là PATIENT");
        }
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
    }

    private boolean hasRole(List<Role> roles, RoleName roleName) {
        return roles.stream().anyMatch(role -> role.getRoleName() == roleName);
    }

    private void saveUserRoles(User user, List<Role> roles) {
        List<UserRole> userRoles = roles.stream()
                .map(role -> {
                    UserRole userRole = new UserRole();
                    userRole.setId(new UserRoleId(user.getId(), role.getId()));
                    userRole.setUser(user);
                    userRole.setRole(role);
                    return userRole;
                })
                .toList();
        userRoleRepository.saveAll(userRoles);
    }

    private List<Role> findRolesByUserId(Long userId) {
        return userRoleRepository.findByUserIdWithRole(userId).stream()
                .map(UserRole::getRole)
                .sorted(Comparator.comparing(role -> role.getRoleName().name()))
                .toList();
    }

    private UserResponse toResponse(User user, List<Role> roles) {
        Long doctorId = user.getDoctor() == null ? null : user.getDoctor().getId();
        Long patientId = user.getPatient() == null ? null : user.getPatient().getId();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getIsActive(),
                roles.stream().map(role -> role.getRoleName().name()).toList(),
                doctorId,
                patientId,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }
}
