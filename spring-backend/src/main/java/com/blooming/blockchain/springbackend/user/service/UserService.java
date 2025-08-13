package com.blooming.blockchain.springbackend.user.service;

import com.blooming.blockchain.springbackend.user.entity.User;
import com.blooming.blockchain.springbackend.user.repository.UserRepository;
import com.blooming.blockchain.springbackend.userdetail.service.UserPointTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserPointTokenService userPointTokenService;

    // Create or update user from OAuth2 login
    public User createOrUpdateUser(String googleId, String email, String name, String avatar) {
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            boolean updated = false;

            if (!email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            if (!name.equals(user.getName())) {
                user.setName(name);
                updated = true;
            }
            if (avatar != null && !avatar.equals(user.getAvatar())) {
                user.setAvatar(avatar);
                updated = true;
            }

            if (updated) {
                user = userRepository.save(user);
                log.info("Updated existing user: {} ({})", name, email);
            } else {
                log.debug("No updates needed for existing user: {} ({})", name, email);
            }

            return user;
        } else {
            // Create new user
            User newUser = User.builder()
                    .googleId(googleId)
                    .email(email)
                    .name(name)
                    .avatar(avatar)
                    .roleId((byte) 2) // Default USER role
                    .build();

            newUser = userRepository.save(newUser);

            // Initialize user point balance (0, 0, 0)
            userPointTokenService.getOrCreateUserBalance(googleId);

            return newUser;
        }
    }

    // Find user by Google ID
    @Transactional(readOnly = true)
    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    // Find user by email
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    // Get all users (admin function)
    @Transactional(readOnly = true)
    public Iterable<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Update user profile
    public User updateUserProfile(String googleId, String name, String avatar) {
        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new RuntimeException("User not found: " + googleId));

        boolean updated = false;
        
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            updated = true;
        }
        
        if (avatar != null && !avatar.equals(user.getAvatar())) {
            user.setAvatar(avatar);
            updated = true;
        }

        if (updated) {
            user = userRepository.save(user);
            log.info("Updated user profile for: {} ({})", user.getName(), user.getEmail());
        }

        return user;
    }

    // Check if user exists
    @Transactional(readOnly = true)
    public boolean userExists(String googleId) {
        return userRepository.existsByGoogleId(googleId);
    }

    // Get user role
    @Transactional(readOnly = true)
    public Byte getUserRole(String googleId) {
        return userRepository.findByGoogleId(googleId)
                .map(User::getRoleId)
                .orElse((byte) 2); // Default USER role
    }

    // Check if user is admin
    @Transactional(readOnly = true)
    public boolean isAdmin(String googleId) {
        return getUserRole(googleId) == 1; // Assuming 1 = ADMIN role
    }

    // Delete user (admin function)
    @Transactional
    public boolean deleteUser(String googleId) {
        Optional<User> user = userRepository.findByGoogleId(googleId);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            log.info("Deleted user: {} ({})", user.get().getName(), user.get().getEmail());
            return true;
        }
        return false;
    }

    // Get user statistics
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics(String googleId) {
        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new RuntimeException("User not found: " + googleId));

        // Get point balances
        Integer mainPoints = userPointTokenService.getMainPointBalance(googleId);
        Integer subPoints = userPointTokenService.getSubPointBalance(googleId);
        Long tokenBalance = userPointTokenService.getTokenBalance(googleId);

        return new UserStatistics(user, mainPoints, subPoints, tokenBalance);
    }

    // Inner class for user statistics
    public static class UserStatistics {
        private final User user;
        private final Integer mainPoints;
        private final Integer subPoints;
        private final Long tokenBalance;

        public UserStatistics(User user, Integer mainPoints, Integer subPoints, Long tokenBalance) {
            this.user = user;
            this.mainPoints = mainPoints;
            this.subPoints = subPoints;
            this.tokenBalance = tokenBalance;
        }

        // Getters
        public User getUser() { return user; }
        public Integer getMainPoints() { return mainPoints; }
        public Integer getSubPoints() { return subPoints; }
        public Long getTokenBalance() { return tokenBalance; }
    }
}