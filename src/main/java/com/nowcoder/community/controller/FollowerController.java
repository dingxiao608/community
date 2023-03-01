package com.nowcoder.community.controller;

import com.nowcoder.community.Event.EventProducer;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowerService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowerController implements CommunityConstant {

    @Autowired
    private FollowerService followerService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    // 关注
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        // 这里要通过拦截器（LoginRequiredInterceptor）检查一下是否登录，跟之前拦截器那一部分内容一样。
        User user = hostHolder.getUser();

        followerService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId); // 点赞的entityUserId就是entityId
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    // 取关
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followerService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    // 关注者列表(userId是你要查看的那个人的id，即当前用户)
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        // 设置当前用户到model
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 设置分页信息
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followerService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        // 查询关注者，并且判断登录用户（不是当前用户）与关注者的“关注状态”
        List<Map<String, Object>> followees = followerService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followees != null){
            for (Map<String, Object> map : followees) {
                User u = (User) map.get("user"); // 被关注用户
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", followees);

        return "site/followee";
    }

    // 粉丝列表(userId是你要查看的那个人的id)
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        // 设置当前用户到model
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 设置分页信息
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followerService.findFollowerCount(userId, ENTITY_TYPE_USER));

        // 查询关注者，并且判断登录用户与粉丝的“关注状态”
        List<Map<String, Object>> followers = followerService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followers != null){
            for (Map<String, Object> map : followers) {
                User u = (User) map.get("user"); // 粉丝
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", followers);

        return "site/follower";
    }

    // 判断登录用户是否关注了userId用户
    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followerService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

}
