package workspace;

import config.AOPConfig;
import config.AppConfig;
import config.JedisFactory;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
//        ApplicationContext ac = new AnnotationConfigApplicationContext();
//        ((AnnotationConfigApplicationContext) ac).register(AppConfig.class);
//        ((AnnotationConfigApplicationContext) ac).refresh();
//        SecKill secKill = ac.getBean(SecKill.class);
//        secKill.seckill("1", 10001L);
        Jedis jedis = new Jedis("120.78.123.91");
        jedis.auth("LNWredis");
        jedis.setnx("100", "100");
        jedis.expire("100", 5);
        jedis.close();
        System.out.println("finish");
    }





    @Test
    public void testSecKill(){

        final ApplicationContext ac = new AnnotationConfigApplicationContext();
        ((AnnotationConfigApplicationContext) ac).register(AppConfig.class);
        ((AnnotationConfigApplicationContext) ac).refresh();
        System.out.println("获取SecKill  Bean");


        int threadCount = 100;
        int splitPoint = threadCount / 2;
         final CountDownLatch endCount = new CountDownLatch(threadCount);
         final CountDownLatch beginCount = new CountDownLatch(1);

        Thread[] threads = new Thread[threadCount];
        //起500个线程，秒杀第一个商品
        for(int i= 0;i < splitPoint;i++){
            threads[i] = new Thread(new  Runnable() {
                @Override
                public void run() {
                    try {
                        //等待在一个信号量上，挂起
                        beginCount.await();
                        SecKill secKill = ac.getBean(SecKill.class);
                        // 该方法会被AOP代理
                        secKill.seckill("1", 10001L);

                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        endCount.countDown();
                    }
                }
            });
            threads[i].start();

        }
        //再起500个线程，秒杀第二件商品
        for(int i= splitPoint;i < threadCount;i++){
            threads[i] = new Thread(new  Runnable() {
                @Override
                public void run() {
                    try {
                        //等待在一个信号量上，挂起
                        beginCount.await();
                        SecKill secKill = ac.getBean(SecKill.class);
                        // 该方法会被AOP代理
                        secKill.seckill("1", 10002L);

                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        endCount.countDown();
                    }
                }
            });
            threads[i].start();

        }


        long startTime = System.currentTimeMillis()/1000;
        //主线程释放开始信号量，并等待结束信号量，这样做保证1000个线程做到完全同时执行，保证测试的正确性
        // 也就说开始让线程工作
        beginCount.countDown();

        try {
            //主线程等待结束信号量，也就是等待其他线程的工作全部做完
            endCount.await();
            //观察秒杀结果是否正确
            System.out.println(SecKillImpl.inventory.get(10001L));
            System.out.println(SecKillImpl.inventory.get(10002L));
            System.out.println("error count" + AOPConfig.ERROR_COUNT);
            System.out.println("total cost " + (System.currentTimeMillis()/1000 - startTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOne() throws InterruptedException {
        final Jedis jedis = new JedisFactory().getJedis();


        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(jedis.setnx("111111", "1"));
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(jedis.setnx("111111", "1"));
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(jedis.setnx("111111", "1"));
            }
        }).start();

        Thread.sleep(2000L);
        System.out.println(jedis.del("111111"));
        jedis.close();

        Long start = System.currentTimeMillis() / 1000;
        Thread.sleep(5000L);
        Long end = System.currentTimeMillis() / 1000;
        System.out.println(end - start);

    }

    @Test
    public void testPrototype(){
        ApplicationContext ac = new AnnotationConfigApplicationContext();
        ((AnnotationConfigApplicationContext) ac).register(AppConfig.class);
        ((AnnotationConfigApplicationContext) ac).refresh();
        System.out.println(ac.getBean(JedisLock.class).toString());
        System.out.println(ac.getBean(JedisLock.class).toString());
        System.out.println(ac.getBean(JedisLock.class).toString());

        System.out.println(ac.getBean(JedisLock.class).flag);

    }
}
