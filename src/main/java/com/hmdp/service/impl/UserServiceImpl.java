package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号格式是否正确
        if (RegexUtils.isPhoneInvalid(phone)){
            // 不正确返回错误信息
            return Result.fail("手机号格式不正确!");
        }
        // 正确则生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 保存验证码到session
        session.setAttribute("code",code);

        // 发送验证码
        log.debug("发送验证码成功，验证码：{}",code);

        // 返回ok
        return Result.ok();

    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)){
            // 不正确返回错误信息
            return Result.fail("手机号格式不正确!");
        }
        // 校验验证码
        Object cacheCode = session.getAttribute(SystemConstants.USER_CODE);
        String code = loginForm.getCode();
        String storedCode = cacheCode == null ? "" : cacheCode.toString();
        if (!storedCode.equals(code)){
            // 验证码有误，报错
            return Result.fail("验证码信息有误");
        }
        // 校验通过，根据手机号查询用户 select * from tb_user where phone = ?
        User user = query().eq(SystemConstants.USER_PHONE, phone).one();

        // 判断用户是否存在
        if (user == null){
            // 不存在，创建新用户并保存
            user = createUserWithPhone(phone);
        }

        // 保存用户信息到session中
        session.setAttribute(SystemConstants.USER_NICK_NAME_PREFIX,user);

        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        // 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        // 保存用户
        save(user);
        return user;
    }
}
