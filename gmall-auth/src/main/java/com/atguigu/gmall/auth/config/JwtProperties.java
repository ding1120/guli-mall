package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 读取前缀为auth.jwt的配置文件中的属性
 */
// @EnableConfigurationProperties(JwtProperties.class)
@ConfigurationProperties(prefix = "auth.jwt")
@Data

public class JwtProperties {
    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private Integer expire;
    private String cookieName;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String unick;
    @PostConstruct
    public void init(){
        try {
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);
            //判断公钥，私钥是否有一对为空，有一个为空全部重新生成
            if(!pubFile.exists()||!priFile.exists()){
                RsaUtils.generateKey(pubKeyPath, priKeyPath,secret);
            }
            this.publicKey=RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey=RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
