package com.yaprj.repository;

import com.yaprj.entity.User;
import com.yaprj.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);
    boolean existsByEmail(String email);
}
