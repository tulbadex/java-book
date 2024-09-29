package demo.service;

import demo.model.User;
import demo.repository.UserRepository;
import demo.request.LoginRequest;
import demo.request.RegisterRequest;
import demo.response.LoginResponse;
import demo.response.RegisterResponse;
import demo.response.ApiResponse;
import demo.security.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByEmail(loginRequest.getEmail());
            String token = jwtUtil.generateToken(userDetails);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setUsername(user.getUsersname());
            loginResponse.setEmail(user.getEmail());
            loginResponse.setFirstName(user.getFirstName());
            loginResponse.setLastName(user.getLastName());
            loginResponse.setToken(token);
            loginResponse.setExpiresIn(jwtUtil.getExpirationTime());;

            ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(200, "Login successful", loginResponse);
            return ResponseEntity.ok(apiResponse);

        } catch (BadCredentialsException e) {
            ApiResponse<String> apiResponse = new ApiResponse<>(401, "Bad credentials", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        } catch (Exception e) {
            ApiResponse<String> apiResponse = new ApiResponse<>(500, "An error occurred during login", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    public ResponseEntity<?> register(RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getPassword() == null ||
            registerRequest.getEmail() == null || registerRequest.getFirstName() == null ||
            registerRequest.getLastName() == null) {
            ApiResponse<String> response = new ApiResponse<>(400, "All fields are required.", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check if the username or email already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            ApiResponse<String> response = new ApiResponse<>(400, "Username is already taken.", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            ApiResponse<String> response = new ApiResponse<>(400, "Email is already in use.", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        User newUser = new User();
        newUser.setUsersname(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setRoles(userService.getDefaultRoles());

        userService.saveUser(newUser);

        // Prepare response object
        List<String> roles = newUser.getRoles().stream()
                                    .map(role -> role.getName()) // Assuming Role is an entity with a 'name' field
                                    .collect(Collectors.toList());
        
        RegisterResponse registerResponse = new RegisterResponse(
            newUser.getFirstName(),
            newUser.getLastName(),
            newUser.getEmail(),
            roles
        );

        ApiResponse<RegisterResponse> response = new ApiResponse<>(200, "User registered successfully.", registerResponse);
        return ResponseEntity.ok(response);
    }
}
