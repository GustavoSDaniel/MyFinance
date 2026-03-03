package com.gustavosdaniel.myfinance_api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Value("${app.security.admin-emails}")
    private  List<String> adminEmails;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");

        List<GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());

        if (email != null && adminEmails.contains(email)) {
            log.info("ACESSO CONCEDIDO: O e-mail {} foi identificado como ADMIN.", email);
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            log.warn("ACESSO PADRÃO: O e-mail {} não está na lista de admins. " +
                    "Atribuindo ROLE_USER.", email);
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "name");
    }
}
