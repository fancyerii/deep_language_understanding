package com.github.fancyerii.deepnlu.dialog.session.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.github.fancyerii.deepnlu.dialog.session.data.DialogRound;
import com.github.fancyerii.deepnlu.dialog.session.data.DialogSession;
import com.github.fancyerii.deepnlu.tools.json.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DialogSessionService {
    private static final int LAST_TIMEOUT = 30;
    private static final int LOCKER_EXPIRE_TIME = 2 * 60;
    private static final int MAX_ROUND_TOKEEP = 10;
    private static String KEY_TEMPLATE = "ai:deepnlu:dialogsession:%s:%s";
    private static String LOCK_TEMPLATE = "ai:deepnlu:dialogsession:%s:%s:lock";
    @Autowired
    private StringRedisTemplate jedis; 

    public void delSession(String robotId, String userId) {
        String key = String.format(KEY_TEMPLATE, robotId, userId);
        jedis.delete(key);
    }

    public DialogSession getOrCreateSession(String robotId, String userId) throws IOException{
        String key = String.format(KEY_TEMPLATE, robotId, userId);
        String ctx = jedis.opsForValue().get(key);
        DialogSession dialogSession;
        if (ctx == null) {
        	dialogSession = new DialogSession();
            dialogSession.setSessionId(UUID.randomUUID().toString());
            dialogSession.setRobotId(robotId);
            dialogSession.setUserId(userId);
            dialogSession.setStartTime(new Date());
            String value = JsonUtil.toJsonString(dialogSession);
            jedis.opsForValue().set(key, value);
            jedis.boundValueOps(key).expire(LAST_TIMEOUT, TimeUnit.MINUTES);
        } else {
        	dialogSession = JsonUtil.toBean(ctx, DialogSession.class);
        }
        return dialogSession;
    }

    public String getSessionId(String robotId, String userId) throws IOException {
        String key = String.format(KEY_TEMPLATE, robotId, userId);
        String ctx = jedis.opsForValue().get(key);
        if (ctx == null) {
            return null;
        } else {
            DialogSession session=JsonUtil.toBean(ctx, DialogSession.class);
            return session.getSessionId();
        }
    }

    private ArrayList<DialogRound> truncateRounds(ArrayList<DialogRound> rounds) {
        if (rounds != null && rounds.size() > MAX_ROUND_TOKEEP) {
            ArrayList<DialogRound> truncated = new ArrayList<DialogRound>(MAX_ROUND_TOKEEP);
            for (int i = rounds.size() - MAX_ROUND_TOKEEP; i < rounds.size(); i++) {
                truncated.add(rounds.get(i));
            }
            return truncated;
        } else {
            return rounds;
        }
    }

    public void saveSession(String robotId, String userId, DialogSession dialogSession) throws IOException {
    	if(dialogSession==null) {
    		throw new IllegalArgumentException("dialogSession is null");
    	}
        String key = String.format(KEY_TEMPLATE, robotId, userId);
        ArrayList<DialogRound> rounds = dialogSession.getRounds();
        dialogSession.setRounds(truncateRounds(rounds));
        String value = JsonUtil.toJsonString(dialogSession);
        jedis.opsForValue().set(key, value);
        jedis.boundValueOps(key).expire(LAST_TIMEOUT, TimeUnit.MINUTES);
    }

    public boolean lock(String robotId, String userId) {
        String lockName = String.format(LOCK_TEMPLATE, robotId, userId);
        boolean lockSucess = jedis.boundValueOps(lockName).setIfAbsent("1");
        boolean expireSucess = jedis.boundValueOps(lockName).expire(LOCKER_EXPIRE_TIME, TimeUnit.SECONDS);
        return lockSucess && expireSucess;
    }

    public void unlock(String robotId, String userId) {
        String lockName = String.format(LOCK_TEMPLATE, robotId, userId);
        jedis.delete(lockName);
    }
}
