package com.one.school.controller;

import com.one.school.bean.BaseResponse;

/**
 * Created by lee on 2019/8/17.
 */
public class BaseController {

    protected BaseResponse successResult() {
        BaseResponse response = new BaseResponse();
        response.success();
        return response;
    }

    protected BaseResponse successResult(Object data) {
        BaseResponse response = new BaseResponse();
        response.success(data);
        return response;
    }

    protected BaseResponse errorResult(){
        BaseResponse resp = new BaseResponse();
        resp.fail(null);
        return resp;
    }

}
