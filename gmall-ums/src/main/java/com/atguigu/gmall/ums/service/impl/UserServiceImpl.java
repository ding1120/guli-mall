package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        //判断是否已有信息注册过
        switch(type){
            case 1: queryWrapper.eq("username",data);break;
            case 2: queryWrapper.eq("phone",data);break;
            case 3: queryWrapper.eq("email",data);break;
            default:
                return null;
        }
        return this.count(queryWrapper)==0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        //TODO 1.校验短信验证码

        //2.生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);
        //3.对用户生成的明文密码进行加盐加密
        //md5Hex代表64位加密
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword()+salt));
        //4.新增用户
        userEntity.setLevelId(1L);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setSourceType(1);
        userEntity.setGrowth(1000);
        userEntity.setStatus(0);
        userEntity.setCreateTime(new Date());
        this.save(userEntity);
        //TODO 5.删除redis中的短信验证码
        
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        //1.根据登录名查询用户
        List<UserEntity> userEntities = this.list(new QueryWrapper<UserEntity>().eq("username", loginName).or().eq("email", loginName).or().eq("phone", loginName));
        //2.判断用户是否为空
        if(CollectionUtils.isEmpty(userEntities)){
            return null;
        }

        for (UserEntity userEntity : userEntities) {
            //3.获取该用户的盐，并对用户输入明文密码加盐加密
            password = DigestUtils.md5Hex(password + userEntity.getSalt());
            //4.用户输入的密码(加盐加密后)和数据库中的密码进行比较
            if(StringUtils.equals(password,userEntity.getPassword()));
            return userEntity;
        }

        return null;
    }
}