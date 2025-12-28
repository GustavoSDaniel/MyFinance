package com.gustavosdaniel.myfinance_api.user;


public interface UserService {

    UserInfoResponse createOrUpdateUserFromOAuth(UserRequest request);
}
