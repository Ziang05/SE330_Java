package com.hospital.service.impl;

import com.hospital.dto.request.RegisterPatientRequest;
import com.hospital.dto.response.RegisterPatientResponse;
import com.hospital.entity.*;
import com.hospital.entity.enums.RoleName;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.RoleRepository;
import com.hospital.repository.UserRepository;
import com.hospital.repository.UserRoleRepository;
import com.hospital.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles patient self-registration: creates Patient + User + UserRole atomically.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegisterPatientResponse registerPatient(RegisterPatientRequest request) {
        validateUniqueAccount(request);
        validateUniqueCccd(request.getCccd());

        // Create patient record (or link to existing one via CCCD)
        Patient patient = buildPatient(request);
        Patient savedPatient = patientRepository.save(patient);

        // Create user account linked to the patient record
        User user = buildUser(request, savedPatient);
        User savedUser = userRepository.save(user);

        // Assign PATIENT role
        Role patientRole = roleRepository.findByRoleName(RoleName.PATIENT)
                .orElseThrow(() -> new BusinessException("Role PATIENT is not configured in the system"));

        UserRole userRole = new UserRole();
        userRole.setId(new UserRoleId(savedUser.getId(), patientRole.getId()));
        userRole.setUser(savedUser);
        userRole.setRole(patientRole);
        userRoleRepository.save(userRole);

        return toResponse(savedUser, savedPatient);
    }

    private void validateUniqueAccount(RegisterPatientRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
    }

    private void validateUniqueCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            return;
        }
        patientRepository.findByCccd(cccd).ifPresent(existing -> {
            throw new DuplicateResourceException("A patient with CCCD " + cccd + " already exists");
        });
    }

    private Patient buildPatient(RegisterPatientRequest request) {
        Patient patient = new Patient();
        patient.setFullName(request.getFullName());
        patient.setDob(request.getDob());
        patient.setGender(request.getGender());
        patient.setCccd(request.getCccd());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setBloodType(request.getBloodType());
        patient.setInsuranceNumber(request.getInsuranceNumber());
        return patient;
    }

    private User buildUser(RegisterPatientRequest request, Patient patient) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setIsActive(true);
        user.setPatient(patient);
        return user;
    }

    private RegisterPatientResponse toResponse(User user, Patient patient) {
        return new RegisterPatientResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                patient.getId(),
                patient.getFullName(),
                patient.getDob(),
                patient.getGender(),
                patient.getCccd(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getBloodType(),
                patient.getInsuranceNumber(),
                user.getCreatedAt()
        );
    }
}
