package com.gustavosdaniel.myfinance_api.user;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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

            return userMapper.toUserInfoResponse(userUpdate);
        }

        User newUser = userMapper.toUser(request);
        User savedUser = userRepository.save(newUser);

        return userMapper.toUserInfoResponse(savedUser);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        log.info("Todos os usuário encontrados {}", users.getNumberOfElements());

        if (users.isEmpty()){

            log.info("Nenhum usuário encontrado na busca");

            return Page.empty();
        }

        return users.map(userMapper::toUserResponse);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<UserResponse> getUserByEmail(String email) {

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()){

            log.info("Nenhum usuário foi encontrado com esse mail {}", email);

            return Optional.empty();
        }

        return user.map(userMapper::toUserResponse);

    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        log.info("Usuário com o id: {} encontrado", id);

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
