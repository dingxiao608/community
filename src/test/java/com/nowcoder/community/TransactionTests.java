package com.nowcoder.community;

import com.nowcoder.community.controller.AlphaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTests {

    @Autowired
    private AlphaController alphaController;

    @Test
    public void testSave1(){
        Object o = alphaController.save1();
        System.out.println(o);
    }

    @Test
    public void testSave2(){
        Object o = alphaController.save2();
        System.out.println(o);
    }
}
