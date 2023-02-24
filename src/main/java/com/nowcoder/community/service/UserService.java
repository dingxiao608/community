package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        return userMapper.selectUserById(id);
    }

    //注册逻辑
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        //空值处理（参数、账号、密码、邮箱）
        if (user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //判断用户是否存在（账号、邮箱）
        User u = userMapper.selectUserByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg", "该用户名已存在！");
            return map;
        }
        u = userMapper.selectUserByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));//只需要5位随机字符
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);  //未激活
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);    //注意：这个方法会自动生成用户id，并封装到user对象中

        //发送验证邮件
        Context context = new Context();
        //url=http://localhost:8080/community/activation/用户id/激活码
        String url = domain + contextPath + "/activation" + "/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        context.setVariable("email", user.getEmail());
        String content = templateEngine.process("/mail/activation", context);//调用模板引擎生成模板内容
        mailClient.sendMail(user.getEmail(), "激活账户", content);

        return map;//如果注册都正常，返回的map为null
    }

    //激活三种情况：成功、重复、激活
    public int activation(int userId, String code){
        User user = userMapper.selectUserById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    //登录
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<String, Object>();
        //空值判断
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        //验证账号和状态
        User user = userMapper.selectUserByName(username);
        if (user == null){
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        if (user.getStatus() == 0){
            map.put("usernameMsg", "账号未激活！");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());//存入的是加密后的密码
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg", "登录密码错误！");
            return map;
        }
        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);//登录有效
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());//需要把ticket反馈给客户端浏览器

        return map;
    }

    //退出
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }

    //发送验证码邮件
    public void sendKaptchaMail(String to, String subject, String content){

        mailClient.sendMail(to, subject, content);
    }

    //修改密码
    public Map<String, Object> changePassword(String mail, String newPassword){
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(mail)){
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg", "新密码不能为空！");
            return map;
        }
        User user = userMapper.selectUserByEmail(mail);
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);

        return map;
    }

    //根据ticket查询LoginTicket
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    //修改头像url
    public void changeHeaderUrl(int userId, String headerUrl){
        userMapper.updateHeader(userId, headerUrl);
    }

    public User findUserByName(String username){
        return userMapper.selectUserByName(username);
    }
}
