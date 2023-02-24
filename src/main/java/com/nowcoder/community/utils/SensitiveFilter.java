package com.nowcoder.community.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //根据敏感词，初始化前缀树
    @PostConstruct
    public void init(){
        try (
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");//is是字节流
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));//对于字符串，new InputStreamReader(is)将字节流转化为字符流，然后再转为缓冲流
        ) {
            //敏感词添加到前缀树中
            String keyword;
            while ((keyword = reader.readLine()) != null){
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过虑的文本
     * @return  返回值是过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)) {
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()){
            char c = text.charAt(position);

            //跳过符号（比如有的是赌博，有的是赌★博这种）
            if (isSymbol(c)){
                //若指针1处于根节点（当指针2和指针3处于同一个位置，即找到了敏感词的开头，开始匹配时，指针1必处于根节点），将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头还是在中间，指针3都向下走一步
                position++;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                //以begin开头的字符串不是敏感词，将begin对应的字符添加到字符串中
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeyWordEnd()){
                //发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            }else {
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符计入结果（比如最后字符串的最后一段不是敏感词）
        sb.append(text.substring(begin));

        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //CharUtils.isAsciiAlphanumeric判断当前是否为普通字符。0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //将敏感词添加到前缀树
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                //如果前缀树没有该字符，初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            //如果前缀树有该字符，指向子节点，进行下一轮循环
            tempNode = subNode;

            //设置结束标识
            if (i == keyword.length() - 1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    //前缀树构建（内部类）
    public class TrieNode{

        //关键词结束标识
        private boolean isKeyWordEnd = false;

        //子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
