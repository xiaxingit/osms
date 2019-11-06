package com.one.school.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

/**
 * Created by lee on 2019/9/1.
 */

@Data
public class StudentIdentityTemplate extends BaseRowModel {

    @ExcelProperty(index = 0)
    private String studentId;

    @ExcelProperty(index = 1)
    private String studentName;

    @ExcelProperty(index = 2)
    private String identityId;
}
