package demo.service;

import demo.model.PasswordResetToken;
import demo.model.Role; // Ensure you have the Role class created
import demo.model.User;
import demo.repository.UserRepository;
import demo.repository.PasswordResetTokenRepository;
import demo.repository.RoleRepository;
import demo.request.UpdateUserProfileRequest;
import demo.response.ApiResponse;
import demo.utils.ApiResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public ApiResponse<String> sendPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return new ApiResponse<>(400, "User with this email doesn't exist.", null);
        }

        // Create and save the password reset token
        User user = userOptional.get();
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user.getId(), calculateExpiryDate());
        passwordResetTokenRepository.save(resetToken);

        // Send email with the reset link
        try {
            String link = "http://localhost:8080/api/auth/reset-password?token=" + token;
            emailService.sendEmail(user.getEmail(), "Reset Password", "Click here to reset your password: " + link);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error occurred while sending the email.", null);
        }

        return new ApiResponse<>(200, "Password reset link sent to " + email, null);
    }

    public ApiResponse<String> resetPassword(String token, String newPassword) {
        // Find the password reset token from the repository
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);

        ApiResponse<PasswordResetToken> tokenResponse = tokenOptional
            .map(resetToken -> ApiResponseUtil.success("Token found", resetToken))
            .orElseGet(() -> ApiResponseUtil.error("Invalid token", 400));

        // If the token is not valid, return the error response
        if (tokenResponse.getStatusCode() != 200) {
            return ApiResponseUtil.error(tokenResponse.getMessage(), tokenResponse.getStatusCode());
        }

        PasswordResetToken resetToken = tokenResponse.getData();
        
        // Check if the token has expired (implement your own logic if needed)
        if (resetToken.getExpiryDate().before(new Date())) {
            return ApiResponseUtil.error("Token has expired.", 400);
        }

        // Find the user associated with the token
        ApiResponse<User> userResponse = userRepository.findById(resetToken.getUserId())
            .map(user -> ApiResponseUtil.success("User found", user))
            .orElseGet(() -> ApiResponseUtil.notFound("User")); // This will now match ApiResponse<User>

        // If the user is not found, return the error response
        if (userResponse.getStatusCode() != 200) {
            return ApiResponseUtil.error(userResponse.getMessage(), userResponse.getStatusCode());
        }

        User user = userResponse.getData();

        // Update the user's password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate the token after use
        passwordResetTokenRepository.delete(resetToken);
        return ApiResponseUtil.success("Password has been reset successfully.");
    }
    
    public Set<Role> getDefaultRoles() {
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                          .orElseThrow(() -> new RuntimeException("Role not found"));
        roles.add(userRole);
        return roles;
    }


    // Loads a user by username, returns ApiResponse<User>
    // public ApiResponse<User> loadUserByUsername(String username) {
    //     return userRepository.findByUsername(username)
    //         .map(user -> new ApiResponse<>(200, "User found", user))
    //         .orElseGet(() -> new ApiResponse<>(404, "User not found", null));
    // }

    // Load user by email (instead of username)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Method to find user by email (if needed in other parts of the service)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public ApiResponse<User> loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(user -> new ApiResponse<>(200, "User found", user))
            .orElseGet(() -> new ApiResponse<>(404, "User not found", null));
    }

    // Finds user by username or email, returns ApiResponse<User>
    public User findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + identifier));
    }

    public void updatePassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateUserProfile(UpdateUserProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        userRepository.save(user);
    }

    private Date calculateExpiryDate() {
        // Set the expiration time to 24 hours from now
        long ONE_DAY_IN_MILLISECONDS = 86400000L;
        return new Date(System.currentTimeMillis() + ONE_DAY_IN_MILLISECONDS);
    }
}