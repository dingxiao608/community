package com.nowcoder.community;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class BlockingQueueTests {

    public static void main(String[] args) {

        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10); // 队列容量为10
        // 启动一个生产者和三个消费者
        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }

}

class Producer implements Runnable{//继承Runnable实现线程

    private BlockingQueue<Integer> queue;   // 队列里的数据类型是Integer

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            // 生产者每隔20ms生产一条消息，存入阻塞队列
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产：" + queue.size());
            }
        } catch (Exception e){
            e.getStackTrace();
        }
    }
}

class Consumer implements Runnable{

    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            // 消费者每隔随机毫秒，从阻塞队列中取出一条消息
            while (true){
                Thread.sleep(new Random().nextInt(1000)); // 间隔毫秒从0-1000里面随机出，比20ms大的可能性很高，即消费速度更慢
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费：" + queue.size());
            }
        } catch (Exception e){
            e.getStackTrace();
        }
    }
}
