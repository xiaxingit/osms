package com.one.school.util;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * Created by lee on 2019/9/1.
 */
public class OsmsUtil {

    private static int DEFAULT_PAGE_NUM = 1;
    private static int DEFAULT_PAGE_SIZE = 10;

    /**
     * 开启分页查询
     * 默认页码：1
     * 默认每页显示数量：10
     * @param pageNum 页码
     * @param pageSize 每页显示数量
     * @return
     */
    public static Page defaultStartPage(Integer pageNum, Integer pageSize) {
        return PageHelper.startPage(null == pageNum ? DEFAULT_PAGE_NUM : pageNum, null == pageSize ? DEFAULT_PAGE_SIZE : pageSize);
    }

}
