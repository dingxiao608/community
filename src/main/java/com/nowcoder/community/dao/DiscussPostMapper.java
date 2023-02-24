package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //@Param用于给参数取别名。如果方法只有一个参数，并且在映射文件中的<if>标签内使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId); //查询帖子总数

    //添加帖子
    int insetDiscussPost(DiscussPost discussPost);

    //根据id查询帖子
    DiscussPost selectDiscussPostById(int id);

    //修改帖子评论数
    int updateCommentCount(int id, int commentCount);
}
