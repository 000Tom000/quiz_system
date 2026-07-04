package com.tom.quiz.service;

import com.tom.quiz.dto.LoginRequest;
import com.tom.quiz.dto.RegisterRequest;
import com.tom.quiz.dto.UserVO;

public interface UserService {

    UserVO register(RegisterRequest req);

    UserVO login(LoginRequest req);

    void sendEmailCode(String email, String purpose);

    void resetPassword(String email, String code, String newPassword);

    UserVO getProfile(Long userId);

    void updatePassword(Long userId, String oldPwd, String newPwd);

    void updateEmail(Long userId, String email, String code);
}
