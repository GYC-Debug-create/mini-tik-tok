package com.minitiktok.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minitiktok.entity.RequestLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLog> {
}
