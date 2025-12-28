package com.gustavosdaniel.myfinance_api.user;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

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
            user.setEmail(request.email());
            user.setName(request.name());
            user.setPicture(request.picture());

            User userUpdate = userRepository.save(user);

            return userMapper.toUserInfoResponse(userUpdate);
        }

        User newUser = userMapper.toUser(request);
        User savedUser = userRepository.save(newUser);

        return userMapper.toUserInfoResponse(savedUser);
    }
}
