package com.one.school.service.user;

import com.one.school.dao.UserMapper;
import com.one.school.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by lee on 2019/8/16.
 */

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public boolean login(String account, String password) {
        User user = userMapper.selectByAccount(account);
        if(null == user || !user.getLoginPassword().equals(password)){
            return false;
        }
        return true;
    }

}
