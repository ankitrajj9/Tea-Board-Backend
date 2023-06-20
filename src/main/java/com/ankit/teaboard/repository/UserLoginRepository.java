package com.ankit.teaboard.repository;

import com.ankit.teaboard.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLoginRepository extends JpaRepository<UserLogin,Long> {

    @Query("SELECT userLogin FROM UserLogin userLogin WHERE userLogin.loginId=:loginId")
    public UserLogin getUserLoginByLoginId(@Param("loginId") String loginId);
}
