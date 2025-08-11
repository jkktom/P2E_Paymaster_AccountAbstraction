package com.blooming.blockchain.springbackend.user.repository;

import com.blooming.blockchain.springbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find user by Google ID (primary lookup)
    Optional<User> findByGoogleId(String googleId);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by smart wallet address
    Optional<User> findBySmartWalletAddress(String smartWalletAddress);

    // Check if user exists by Google ID
    boolean existsByGoogleId(String googleId);

    // Check if user exists by email
    boolean existsByEmail(String email);

    // Check if smart wallet address is already taken
    boolean existsBySmartWalletAddress(String smartWalletAddress);

    // Find users by role
    List<User> findByRoleId(Byte roleId);

    // Find all admin users
    @Query("SELECT u FROM User u WHERE u.roleId = 1")
    List<User> findAdminUsers();

    // Find all regular users
    @Query("SELECT u FROM User u WHERE u.roleId = 2")
    List<User> findRegularUsers();

    // Find users created after a certain date
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedAfter(@Param("date") java.time.LocalDateTime date);

    // Count total users
    long count();

    // Count users by role
    long countByRoleId(Byte roleId);

    // Search users by name (case insensitive)
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    // Search users by email domain
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<User> findByEmailDomain(@Param("domain") String domain);
}