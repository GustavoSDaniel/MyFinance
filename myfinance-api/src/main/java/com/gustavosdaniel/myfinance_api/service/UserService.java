package com.gustavosdaniel.myfinance_api.service;

import com.gustavosdaniel.myfinance_api.domain.dto.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.UserRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.UserResponse;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import com.gustavosdaniel.myfinance_api.domain.mapping.UserMapper;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.exception.UserNotFoundException;
import com.gustavosdaniel.myfinance_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@CacheConfig(cacheNames = "users")
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${app.security.admin-emails}")
    private  List<String> adminEmails;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public UserInfoResponse createOrUpdateUserFromOAuth(UserRequest request) {

        Optional<User> existingUser = userRepository.findByEmail(request.email());

        if (existingUser.isPresent()) {

            User user = existingUser.get();
            user.setName(request.name());
            user.setPicture(request.picture());

            User userUpdate = userRepository.save(user);

            log.info("Usuário: {} atualizado com sucesso", userUpdate.getName());

            return userMapper.toUserInfoResponse(userUpdate);
        }
        UserRole role = UserRole.USER;

        if (adminEmails.contains(request.email())){

            role = UserRole.ADMIN;
        }

        User newUser = userMapper.toUser(request);
        newUser.setRole(role);
        User savedUser = userRepository.save(newUser);

        log.info("Novo usuário: {} salvo com sucesso", savedUser.getName());

        return userMapper.toUserInfoResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        if (users.isEmpty()){

            log.info("Nenhum usuário encontrado na busca");

            return ResponseEntity.noContent().build();
        }

        log.info("Todos os usuário encontrados {}", users.getNumberOfElements());

        return ResponseEntity.ok(users.map(userMapper::toUserResponse));

    }


    @Transactional(readOnly = true)
    @Cacheable(key = "#email", unless = "#result == null")
    public ResponseEntity<UserResponse> getUserByEmail(String email) {

        Optional<User> user = userRepository.findByEmail(email);

        log.info("Buscando usuário pelo email {}", email);

        if (user.isEmpty()){

            log.warn("Nenhum usuário foi encontrado com esse mail {}", email);

            return ResponseEntity.noContent().build();
        }

        log.info("Usuário com o email {}, encontrado com sucesso", user.get().getEmail());

        UserResponse response = userMapper.toUserResponse(user.get());

        return ResponseEntity.ok(response);

    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public ResponseEntity<UserResponse> getUserById(UUID id) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        log.info("Usuário com o id: {} encontrado com sucesso", id);

        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public ResponseEntity<Void> deleteUser(UUID id, Authentication authentication) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ADMIN"));

        if (isAdmin){

            userRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        String emailLogado = authentication.getName();

        if (!isAdmin && !user.getEmail().equals(emailLogado)) {

            log.warn("Usuário {} tentou deletar a conta de {}", emailLogado, user.getEmail());

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userRepository.delete(user);

        log.info("Usuário {} deletado com sucesso", user.getName());

        return ResponseEntity.noContent().build();
    }

}
