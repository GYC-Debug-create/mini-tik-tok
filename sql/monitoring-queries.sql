-- 答辩 / 联调：request_logs 监控查询示例（REQ-LOG-001/002、技术设计 §8.1）
USE douyin_lite;

-- 最近 50 条请求日志（含输入/输出摘要、耗时）
SELECT id, user_id, method, path, request_params, response_code,
       LEFT(response_body, 120) AS response_body_preview, cost_ms, created_at
FROM request_logs
ORDER BY created_at DESC
LIMIT 50;

-- 按路径聚合：平均耗时与调用次数（集成监控展示）
SELECT path,
       AVG(cost_ms) AS avg_cost_ms,
       COUNT(*)     AS request_count
FROM request_logs
GROUP BY path
ORDER BY request_count DESC;
