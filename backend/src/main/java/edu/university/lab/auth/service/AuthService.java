package edu.university.lab.auth.service;

import edu.university.lab.auth.dto.LoginRequest;
import edu.university.lab.auth.dto.LoginResponse;
import edu.university.lab.auth.dto.AuthContextResponse;
import edu.university.lab.auth.dto.UserProfile;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserProfile currentUser();

    AuthContextResponse currentContext();
}
