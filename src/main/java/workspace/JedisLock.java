package workspace;

import config.JedisFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;


@Component
@Scope("prototype")
public class JedisLock implements InitializingBean {

    @Autowired
    JedisFactory jedisFactory;


    Jedis jedis;

    /**
     * 该标志用于判断是否需要删除锁
     */
    boolean flag = false;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 每个Jedis锁对应一个Jedis连接
        // 只有最后调用unlock，才释放Jedis连接
        this.jedis = jedisFactory.getJedis();
    }

    public boolean lock(Long gId, long timeout, int expire) {
        try {
            Long startTime = System.currentTimeMillis() / 1000;
            // 在限定时间内有限循环轮询
            while (System.currentTimeMillis() / 1000 - startTime < timeout) {
                // 如果获取锁成功
                Long result = 0L;
                try {
                    // 调用redis的setnx
                    result = jedis.setnx(gId.toString(), "1");
                }catch (Exception e){
                    continue;
                }
                if (result == 1) {
                    System.out.println(Thread.currentThread().getId() + "获取"+ gId +"锁成功");
                    // 设置过期时间
                    jedis.expire(gId + "", expire);
                    this.flag = true;
                    return true;
                }
                // 如果获取不到锁
                System.out.println(Thread.currentThread().getId() + "出现锁等待");
                // 短暂休眠，尽可能的避免活锁
                Thread.sleep(200);
            }
            return false;
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getId()+ "获取锁"+gId+"错误");
            e.printStackTrace();
            return false;
        }
    }

    public void unlock(Long gId) {
        try {
            if (this.flag){
                // 直接删除
                jedis.del(gId + "");
                System.out.println(Thread.currentThread().getId()+ "删除" + gId +"锁成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (jedis.isConnected()) {
                jedis.close();
            }
        }
    }
}
