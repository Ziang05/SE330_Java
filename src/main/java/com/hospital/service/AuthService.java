package com.hospital.service;

import com.hospital.dto.request.RegisterPatientRequest;
import com.hospital.dto.response.RegisterPatientResponse;

/**
 * Authentication-related application operations (registration, etc.).
 * Login/refresh/logout are handled directly in AuthController via Spring Security.
 */
public interface AuthService {

    RegisterPatientResponse registerPatient(RegisterPatientRequest request);
}
