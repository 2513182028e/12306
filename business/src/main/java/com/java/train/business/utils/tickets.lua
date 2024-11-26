-- 参数:
-- KEYS[1]: key (日期 + 车次编号 + 座位类型)
-- KEYS[2]: 用户标识或请求唯一标识符 (如用户 ID 或请求 ID)

local key = KEYS[1]
local userKey = KEYS[2]  -- 用用户 ID 或请求唯一标识符来避免重复秒杀

-- 检查是否已经存在该用户的秒杀标记
if redis.call('SISMEMBER', key, userKey) == 1 then
    return 0  -- 如果已经记录了该用户的请求，则表示重复秒杀，返回失败
end

-- 获取剩余库存
local currentSeats = tonumber(redis.call('GET', key))
if currentSeats and currentSeats > 0 then
    -- 库存足够，扣减库存
    redis.call('DECR', key)

    -- 将用户标识加入集合中，标记该用户已进行秒杀
    redis.call('SADD', key, userKey)

    -- 设置该用户标记的过期时间，防止用户长时间占用标记
    redis.call('EXPIRE', key, 60)  -- 假设过期时间为 60 秒

    return 1  -- 秒杀成功
else
    return 0  -- 库存不足，秒杀失败
end
