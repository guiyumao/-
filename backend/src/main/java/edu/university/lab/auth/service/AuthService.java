package edu.university.lab.auth.service;

import edu.university.lab.auth.dto.AuthContextResponse;
import edu.university.lab.auth.dto.LoginRequest;
import edu.university.lab.auth.dto.LoginResponse;
import edu.university.lab.auth.dto.RegisterRequest;
import edu.university.lab.auth.dto.RegisterResponse;
import edu.university.lab.auth.dto.UserProfile;
import edu.university.lab.module.role.entity.Role;
import java.util.List;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    UserProfile currentUser();

    AuthContextResponse currentContext();

    List<Role> availableRoles(String username);
}
