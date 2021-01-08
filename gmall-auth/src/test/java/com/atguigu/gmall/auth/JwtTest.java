package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "F:\\guli-front\\rsa\\rsa.pub";
    private static final String priKeyPath = "F:\\guli-front\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

   @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "1l");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjFsIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MDk1MDQxNTR9.YeV9n-G8HAwpo2rXZbAWuplBoVmRTHLJxRGU4LNPGcmJAOpnG-WVo-leQCVt107i4RQT5Ci87Rw7td4DweMn-Z5xmEKdR2T9BOmH5NBodWmI2DivR2r64fk-2kDRjxwMy5ecLTUALnWRmsW5N8ijigBBWb76p6uQuH6jsR2_Z-xIxyPCmn2XOdc7rh7dvdV6cNAbzDL7ZzMhWYvuOyXZfNN6dA_r0Asw9RtIKSzVyqDDp16miLQ5LvRqiGYqhuAemStWi_xfiFoQLXZXGz_lLPgoJChzyMdoiEN9UQ0FNRxm3zlRJfG-KH0-ZtT8V0chj5b9SgMeMHvSRcZavmBq4g";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}