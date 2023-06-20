package com.ankit.teaboard.service;

import com.ankit.teaboard.repository.UserLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service("userDetailsService")
public class UserLoginAuthService implements UserDetailsService {

    @Autowired
    private UserLoginRepository userLoginRepository;


    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return userLoginRepository.getUserLoginByLoginId(loginId);
    }
}