package com.minitiktok.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一分页响应（技术设计 §5.1），供成员 C 等列表接口使用。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private int page;
    private int pageSize;
    private long total;
    private List<T> items;

    public static <T> PageResult<T> of(int page, int pageSize, long total, List<T> items) {
        return new PageResult<>(page, pageSize, total, items);
    }
}
