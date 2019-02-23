package workspace;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SecKillImpl implements SecKill {
    /**
     * 模拟数据库
     */
    static Map<Long, Long> inventory ;
    static {
        inventory = new HashMap<>();
        // 插入两条数据
        inventory.put(10001L, 1000L);
        inventory.put(10002L, 1000L);
    }

    @Override
    public void seckill(String userId, Long gId) {
        if (null!=userId){
            reduceInventory(gId);
        }
    }


    /**
     * 库存减一
     * @param gId
     * @return
     */
    public Long reduceInventory(Long gId){
        inventory.put(gId, inventory.get(gId)-1);
        return  inventory.get(gId);
    }
}
