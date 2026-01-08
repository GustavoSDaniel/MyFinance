package com.gustavosdaniel.myfinance_api.user;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${app.security.admin-emails}")
    private  List<String> adminEmails;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
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

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        if (users.isEmpty()){

            log.info("Nenhum usuário encontrado na busca");

            return Page.empty();
        }

        log.info("Todos os usuário encontrados {}", users.getNumberOfElements());

        return users.map(userMapper::toUserResponse);

    }


    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(String email) {

        Optional<User> user = userRepository.findByEmail(email);

        log.info("Buscando usuário pelo email {}", email);

        if (user.isEmpty()){

            log.warn("Nenhum usuário foi encontrado com esse mail {}", email);

            return Optional.empty();
        }

        log.info("Usuário com o email {}, encontrado com sucesso", user.get().getEmail());

        return user.map(userMapper::toUserResponse);

    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        log.info("Usuário com o id: {} encontrado com sucesso", id);

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);

        log.info("Usuário {} deletado com sucesso", user.getName());
    }

}
