package config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;
import workspace.JedisLock;


@Component
@Aspect
public class AOPConfig implements BeanFactoryAware {

    /**
     * 客户端获取锁失败后轮询获取锁的时间30秒
     */
    final static Long TIME_OUT = 30L;
    /**
     * Redis中键的过期时间10秒
     */
    final static int EXPIRE = 10;

    /**
     * 记录失败操作的次数
     */
    public static Long ERROR_COUNT = 0L;

    /**
     * 执行秒杀方法时(载点)
     */
    @Pointcut("execution(* workspace.SecKillImpl.seckill(..))")
    public void pointCut(){

    }

    @Around("pointCut()")
    public boolean around(ProceedingJoinPoint pjp) throws Throwable {
        Object[] objects = pjp.getArgs();
        Long gId = (Long) objects[1];
        JedisLock lock = null;
        try {
            System.out.println(Thread.currentThread().getId()+" before executing--");
            lock = createJedisLock();
            if (lock.lock(gId,TIME_OUT, EXPIRE)) {
                // lock.setFlag(true);
                pjp.proceed();
                System.out.println("--after executing--");
                return true;
            }
            else {
                System.out.println(Thread.currentThread().getId()+"  操作失败--");
                ERROR_COUNT++;
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            lock.unlock(gId);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    BeanFactory beanFactory;

    public JedisLock createJedisLock(){
        return this.beanFactory.getBean(JedisLock.class);
    }
}
