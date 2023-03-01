package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import com.nowcoder.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    //跳转到注册页面
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    //跳转到登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    //注册：提交注册表单后
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        //注册成功，返回到index页面；注册失败，回到register页面，并展示错误消息。
        if (map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");//跳转到主页
            return "site/operate-result";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    //激活。url=http://localhost:8080/community/activation/用户id/激活码
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int res = userService.activation(userId, code);
        if (res == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        }else if (res == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已经激活过了！");
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }

    //生成验证码，并存储在服务器的session里
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session（session存在服务器端，方便客户端输入验证码时来对比）
        //session.setAttribute("kaptcha", text);

        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60); // 60s过期
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS); // redis中设置该key60s过期

        //将图片输出给浏览器
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }

    //登录
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,//code验证码，rememberme页面是否勾选“记住我”
                        Model model, /*HttpSession session, */HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){//session要用来验证浏览器输入的验证码是否服务器存储的验证码一样，ticket存入cookie通过response返回给浏览器
        //检查验证码（因为验证码在表现层（网页上可见），所以不写在service里面）
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(code) ||  StringUtils.isBlank(kaptcha) || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("codeMsg", "验证码不正确！");
            return "site/login";
        }
        //调用service登录（先检查验证码，再访问数据库，效率更高）
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //判断是否登录成功，并分别处理
        boolean containsKey = map.containsKey("ticket");//如果存在key为"ticket"则登陆成功
        if (containsKey){
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);//ticket发送客户端
            return "redirect:/index";//重定向到首页
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    //退出登录
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }

    //忘记密码页面
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage(){
        return "site/forget";
    }

    //修改密码——发送验证码部分：在假设能够获取邮箱的情况下
    @RequestMapping(path = "/getCode", method = RequestMethod.POST)
    @ResponseBody
    public void getCode(HttpSession session, String email){
        //先判断邮箱是否存在

        String kaptcha = kaptchaProducer.createText();
        session.setAttribute("kaptcha", kaptcha);

        userService.sendKaptchaMail(email, "修改密码", "修改密码所需要的验证码为：" + kaptcha);
    }

    //修改密码——根据表单提交的数据修改密码
    @RequestMapping(path = "/changePassword", method = RequestMethod.POST)
    public String changePassword(String email, String code, String newPassword, HttpSession session, Model model){
        System.out.println(newPassword);
        //对比验证码
        if (StringUtils.isBlank(code)){
            model.addAttribute("codeMsg", "验证码不能为空！");
            return "site/forget";
        }
        if (!code.equalsIgnoreCase((String)session.getAttribute("kaptcha"))){
            model.addAttribute("codeMsg", "验证码错误！");
            return "site/forget";
        }

        //修改密码
        Map<String, Object> map = userService.changePassword(email, newPassword);
        if (map.isEmpty()){
            model.addAttribute("msg", "修改密码成功！");
            model.addAttribute("target", "/login");
            return "site/operate-result";
        }else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "site/forget";
        }
    }

}
