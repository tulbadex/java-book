package demo.controller;

import demo.request.ForgotPasswordRequest;
import demo.request.LoginRequest;
import demo.request.RegisterRequest;
import demo.request.ResetPasswordRequest;
import demo.request.UpdatePasswordRequest;
import demo.response.ApiResponse;
import demo.service.AuthService;
import demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    private UserService userService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        ApiResponse<String> response = userService.sendPasswordResetToken(forgotPasswordRequest.getEmail());
        return new ResponseEntity<>(response, response.getStatusCode() == 200 ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
        @RequestBody ResetPasswordRequest resetPasswordRequest,
        @RequestParam String token        
    ) {
        ApiResponse<String> response = userService.resetPassword(token, resetPasswordRequest.getPassword());
        return new ResponseEntity<>(response, response.getStatusCode() == 200 ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/update-password")
    public String updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        userService.updatePassword(updatePasswordRequest.getUserId(), updatePasswordRequest.getNewPassword());
        return "Password updated successfully!";
    }
}