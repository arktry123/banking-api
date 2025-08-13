package com.eagle.banking.controller;

import com.eagle.banking.dto.AuthRequest;
import com.eagle.banking.dto.AuthResponse;
import com.eagle.banking.security.JwtUtil;
import com.eagle.banking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        var oUser = userService.findByUsername(req.getUsername());
        if (oUser.isEmpty() || !oUser.get().getPassword().equals(req.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        String token = jwtUtil.generateToken(oUser.get().getId());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
