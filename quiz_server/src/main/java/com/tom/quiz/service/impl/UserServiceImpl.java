package com.tom.quiz.service.impl;

import com.tom.quiz.dto.LoginRequest;
import com.tom.quiz.dto.RegisterRequest;
import com.tom.quiz.dto.UserVO;
import com.tom.quiz.mapper.*;
import com.tom.quiz.models.*;
import com.tom.quiz.service.UserService;
import com.tom.quiz.utils.CodeUtil;
import com.tom.quiz.utils.MailUtil;
import com.tom.quiz.utils.TokenUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailCodeMapper emailCodeMapper;
    @Autowired
    private LoginAttemptMapper loginAttemptMapper;
    @Autowired
    private PresetStudentMapper presetStudentMapper;
    @Autowired
    private MailUtil mailUtil;

    // ===== 注册 =====
    @Override
    @Transactional
    public UserVO register(RegisterRequest req) {
        // 1. 验证邮箱验证码
        EmailCode ec = emailCodeMapper.findByEmailCode(req.getEmail(), req.getCode(), "register");
        if (ec == null) throw new RuntimeException("验证码错误或已过期");

        // 2. 校验学号姓名
        PresetStudent ps = presetStudentMapper.findByStudentNoAndName(req.getStudentNo(), req.getRealName());
        if (ps == null) throw new RuntimeException("学号或姓名与学校档案不符，请联系管理员");

        // 3. 校验用户名唯一
        if (userMapper.findByUsername(req.getUsername()) != null)
            throw new RuntimeException("用户名已存在");

        // 4. 创建用户（密码 md5 简单加密）
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(md5(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRole("student");
        user.setStudentNo(req.getStudentNo());
        user.setRealName(req.getRealName());
        user.setClassName(ps.getClassName());
        user.setSchoolId(ps.getSchoolId());
        user.setToken(TokenUtil.generate());
        userMapper.insert(user);

        // 5. 标记预设学生已注册、验证码已使用
        presetStudentMapper.markRegistered(ps.getId());
        emailCodeMapper.markUsed(ec.getId());

        return toVO(user);
    }

    // ===== 登录 =====
    @Override
    @Transactional
    public UserVO login(LoginRequest req) {
        User user;

        if ("code".equals(req.getType())) {
            // 验证码登录
            EmailCode ec = emailCodeMapper.findByEmailCode(req.getEmail(), req.getCode(), "login");
            if (ec == null) throw new RuntimeException("验证码错误或已过期");
            user = userMapper.findByEmail(req.getEmail());
            if (user == null) throw new RuntimeException("邮箱未注册");
            emailCodeMapper.markUsed(ec.getId());
        } else {
            // 密码登录
            user = userMapper.findByUsername(req.getUsername());
            if (user == null) throw new RuntimeException("用户名或密码错误");
            if (!md5(req.getPassword()).equals(user.getPassword()))
                throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) throw new RuntimeException("账号已被禁用，请联系管理员");

        // 更新 token
        String token = TokenUtil.generate();
        userMapper.updateToken(user.getId(), token);
        user.setToken(token);

        return toVO(user);
    }

    // ===== 发送验证码 =====
    @Override
    public void sendEmailCode(String email, String purpose) {
        String code = CodeUtil.generate();
        EmailCode ec = new EmailCode();
        ec.setEmail(email);
        ec.setCode(code);
        ec.setPurpose(purpose);
        ec.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        emailCodeMapper.insert(ec);
        mailUtil.sendCode(email, code);
    }

    // ===== 重置密码 =====
    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        EmailCode ec = emailCodeMapper.findByEmailCode(email, code, "reset");
        if (ec == null) throw new RuntimeException("验证码错误或已过期");

        User user = userMapper.findByEmail(email);
        if (user == null) throw new RuntimeException("邮箱未注册");

        userMapper.updatePassword(user.getId(), md5(newPassword));
        emailCodeMapper.markUsed(ec.getId());
    }

    // ===== 个人信息 =====
    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        return toVO(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPwd, String newPwd) {
        User user = userMapper.findById(userId);
        if (!md5(oldPwd).equals(user.getPassword()))
            throw new RuntimeException("原密码错误");
        userMapper.updatePassword(userId, md5(newPwd));
    }

    @Override
    @Transactional
    public void updateEmail(Long userId, String email, String code) {
        EmailCode ec = emailCodeMapper.findByEmailCode(email, code, "modify");
        if (ec == null) throw new RuntimeException("验证码错误或已过期");
        userMapper.updateEmail(userId, email);
        emailCodeMapper.markUsed(ec.getId());
    }

    // ===== 工具 =====
    private UserVO toVO(User u) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(u, vo);
        vo.setClassName(u.getClassName());
        return vo;
    }

    private String md5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
    }
}
