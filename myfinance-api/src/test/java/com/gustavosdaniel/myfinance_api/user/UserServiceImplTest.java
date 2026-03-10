package com.gustavosdaniel.myfinance_api.user;

import com.gustavosdaniel.myfinance_api.domain.dto.UserInfoResponse;
import com.gustavosdaniel.myfinance_api.domain.dto.UserRequest;
import com.gustavosdaniel.myfinance_api.domain.dto.UserResponse;
import com.gustavosdaniel.myfinance_api.domain.enuns.UserRole;
import com.gustavosdaniel.myfinance_api.domain.mapping.UserMapper;
import com.gustavosdaniel.myfinance_api.domain.po.User;
import com.gustavosdaniel.myfinance_api.repository.UserRepository;

import com.gustavosdaniel.myfinance_api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    UserService userService;


    @Nested
    class createUser{

        @Test
        @DisplayName("Should create user with sucesso")
        void shouldCreatedUser(){

            UUID userId = UUID.randomUUID();
            String email = "gustavosdaniel@gmail.com";

            UserRequest request = new UserRequest(
                    "gustavosdaniel@gmail.com", "Gustavo", "fotinha.png");

            User newUser = new User("gustavosdaniel@gmail.com","Gustavo", UserRole.USER );
            ReflectionTestUtils.setField(newUser, "id", userId);

            UserInfoResponse response = new UserInfoResponse(
                    "Gustavo", "gustavosdaniel@gmail.com", "foto.png");

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            when(userMapper.toUser(request)).thenReturn(newUser);
            when(userRepository.save(newUser)).thenReturn(newUser);
            when(userMapper.toUserInfoResponse(newUser)).thenReturn(response);

            UserInfoResponse output = userService.createOrUpdateUserFromOAuth(request);

            assertNotNull(output);


            verify(userMapper).toUser(request);
            verify(userRepository).save(any(User.class));
            verify(userMapper).toUserInfoResponse(newUser);

        }
    }

    @Nested
    class updateUser{

        @Test
        @DisplayName("Should update user with sucesso")
        void shouldUpdateUser(){

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);

            UserRequest request = new UserRequest(
                    "gustavosdaniel@gmail.com", "Eduardo", "image.png");

            UserInfoResponse userInfoResponse = new UserInfoResponse(
                    "Eduardo","gustavosdaniel@gmail.com","imagem2.png");

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserInfoResponse(user)).thenReturn(userInfoResponse);

            UserInfoResponse output = userService.createOrUpdateUserFromOAuth(request);

            assertNotNull(output);

            assertEquals("Eduardo", user.getName());
            assertEquals("image.png", user.getPicture());

            verify(userRepository).findByEmail(user.getEmail());
            verify(userRepository).save(user);
            verify(userMapper).toUserInfoResponse(user);
        }
    }

    @Nested
    class getAllUsers{

        @Test
        @DisplayName("Should all users with sucesso")
        void shouldAllUserss(){

            UUID userId = UUID.randomUUID();
            Pageable pageable = Pageable.unpaged();

            User user1 = new User("gustavosdaniel@gmail.com","Gustavo", UserRole.ADMIN);
            User user2 = new User("silva@gmail.com","Silva", UserRole.USER);
            User user3 = new User("daniel@gmail.com","Daniel", UserRole.USER);

            List<User> users = Arrays.asList(user1, user2, user3);

            Page<User> userPage = new PageImpl<>(users, pageable, users.size());

            UserResponse userResponse1 = new UserResponse(userId,"Gustavo", "gustavosdaniel@gmail.com");
            UserResponse userResponse2 = new UserResponse(userId,"Silva", "silva@gmail.com");
            UserResponse userResponse3 = new UserResponse(userId,"Daniel", "daniel@gmail.com");

            when(userRepository.findAll(pageable)).thenReturn(userPage);

            when(userMapper.toUserResponse(user1)).thenReturn(userResponse1);
            when(userMapper.toUserResponse(user2)).thenReturn(userResponse2);
            when(userMapper.toUserResponse(user3)).thenReturn(userResponse3);

            ResponseEntity<Page<UserResponse>> output = userService.getAllUsers(pageable);

            assertNotNull(output);

            verify(userRepository).findAll(pageable);
            verify(userMapper, times(3)).toUserResponse(any(User.class));

        }
    }

    @Nested
    class getByEmail{

        @Test
        @DisplayName("Should user by email with sucesso")
        void shouldUserByEmail(){

            UUID userId = UUID.randomUUID();
            String email = "gustavosdaniel@gmail.com";

            User user = new User(email,"Gustavo", UserRole.USER);

            UserResponse userResponse = new UserResponse(userId, "Gustavo", "gustavosdaniel@gmail.com");

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            ResponseEntity<UserResponse> output = userService.getUserByEmail(email);

            assertNotNull(output);

            verify(userRepository).findByEmail(user.getEmail());
            verify(userMapper).toUserResponse(user);
        }
    }

    @Nested
    class getUserById{

        @Test
        @DisplayName("Should user by id with sucesso")
        void shouldUserById(){

            UUID userId = UUID.randomUUID();
            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            UserResponse userResponse = new UserResponse(userId,"Gustavo", "gustavosdaniel@gmail.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            ResponseEntity<UserResponse> output = userService.getUserById(userId);

            assertNotNull(output);

            verify(userRepository).findById(userId);
            verify(userMapper).toUserResponse(user);

        }
    }

    @Nested
    class DeleteUser {

        @Test
        @DisplayName("Should delete user successfully when admin")
        void shouldDeleteUserWhenAdmin() {

            UUID userId = UUID.randomUUID();

            User user = new User("gustavosdaniel@gmail.com", "Gustavo", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Authentication authentication = mock(Authentication.class);

            when(authentication.getAuthorities())
                    .thenReturn((Collection) List.of(new SimpleGrantedAuthority("ADMIN")));


            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));

            ResponseEntity<Void> response = userService.deleteUser(userId, authentication);

            verify(userRepository).findById(userId);
            verify(userRepository).deleteById(userId);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }

}