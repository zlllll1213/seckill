-- KEYS[1] : 库存 key（String，值为剩余库存数量）
-- KEYS[2] : 用户秒杀标记 key（Set，存储已参与用户的 ID）
-- ARGV[1] : userId（字符串形式）

-- 1. 检查用户是否已经参与过本次秒杀
local alreadyBought = redis.call('sismember', KEYS[2], ARGV[1])
if alreadyBought == 1 then
    return 1  -- 重复购买
end

-- 2. 检查库存
local stock = tonumber(redis.call('get', KEYS[1]))
if stock == nil or stock <= 0 then
    return 2  -- 库存不足
end

-- 3. 原子扣减库存，并记录用户标记
redis.call('decr', KEYS[1])
redis.call('sadd', KEYS[2], ARGV[1])

return 0  -- 成功
