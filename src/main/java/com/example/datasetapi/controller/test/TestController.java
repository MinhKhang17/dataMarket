package com.example.datasetapi.controller.test;

import com.example.datasetapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class TestController {
    @Autowired
   private JwtUtil jwtUtil ;

    @PreAuthorize("hasRole(USER)")
    @GetMapping("/test/auth")
    public ResponseEntity<String> testToken() {
        System.out.println("testToken");
        return ResponseEntity.ok().body("vao thanh cong voi quyen User");
    }
}
