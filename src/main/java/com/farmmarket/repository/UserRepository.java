package com.farmmarket.repository;

import com.farmmarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    List<User> findByIsActive(Boolean isActive);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveUsersByRole(User.Role role);
}
