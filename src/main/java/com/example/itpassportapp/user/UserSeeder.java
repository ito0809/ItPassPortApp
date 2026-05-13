package com.example.itpassportapp.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

//お試し用アカウント
@Component
public class UserSeeder implements CommandLineRunner {

    private final UserDao userDao;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserSeeder(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void run(String... args) {
        if (userDao.findByEmail("test@example.com") != null) return;

        String hash = encoder.encode("password123");
        userDao.insert("test@example.com", hash, "testUser");

        System.out.println("✅ Seed user created: test@example.com / password123");
    }
}
