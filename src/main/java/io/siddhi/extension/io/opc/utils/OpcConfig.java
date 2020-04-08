package io.siddhi.extension.io.opc.utils;

import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.utils
 * @date 2020/4/8 15:51
 * @Copyright © 2019-2020  QuickSilverDB
 */
public class OpcConfig {

    public OpcConfig() {
    }

  private String opcAppName;
  private String certPath;
  private String privPath;
  private String clientTimeout;
  private String maxMessageLength;
  //None; Sign; SignAndEncrypt;
  private String messageSecurityMode;
  private String opcServerUrl;
   //Basic128Rsa15;Basic256;Basic256Sha256
  private String securityPolicy;
  private String userName;
  private String passWord;

    public Integer getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Integer authentication) {
        this.authentication = authentication;
    }

    //0:匿名;1:验证用户名、密码,2:certificate,3:IssuedToken
  private Integer authentication;

    public String getOpcAppName() {
        return opcAppName;
    }

    public void setOpcAppName(String opcAppName) {
        this.opcAppName = opcAppName;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getPrivPath() {
        return privPath;
    }

    public void setPrivPath(String privPath) {
        this.privPath = privPath;
    }

    public String getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(String clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public String getMaxMessageLength() {
        return maxMessageLength;
    }

    public void setMaxMessageLength(String maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    public String getMessageSecurityMode() {
        return messageSecurityMode;
    }

    public void setMessageSecurityMode(String messageSecurityMode) {
        this.messageSecurityMode = messageSecurityMode;
    }

    public String getOpcServerUrl() {
        return opcServerUrl;
    }

    public void setOpcServerUrl(String opcServerUrl) {
        this.opcServerUrl = opcServerUrl;
    }

    public String getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(String securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
