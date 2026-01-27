package com.upc.smaf.controllers;

import com.upc.smaf.entities.Users;
import com.upc.smaf.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsersController {

    private final IUserRepository usersRepository;

    @GetMapping
    public ResponseEntity<List<Users>> listarTodos() {
        return ResponseEntity.ok(usersRepository.findAll());
    }
}