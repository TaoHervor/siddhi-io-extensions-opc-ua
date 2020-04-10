package io.siddhi.extension.io.opc.utils;

import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.IdType;
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

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(OpcConfig.class);
  private String opcAppName;
  private String certPath;
  private String privPath;
  private String clientTimeout;
  private String maxMessageLength;
  //None; Sign; SignAndEncrypt;
  private MessageSecurityMode messageSecurityMode;
  private String opcServerUrl;
  //BASIC128RSA15;BASIC256;BASIC256SHA256;AES128_SHA256_RSAOAEP;AES256_SHA256_RSAPSS;NONE
  private SecurityPolicy securityPolicy;
  private String userName;
  private String passWord;
  private NodeId nodeId;

    public NodeId getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nameSpaceIndex,String identifier,String idType) {
        switch (idType) {
            case "String":
                this.nodeId=NodeId.get(IdType.String,nameSpaceIndex,identifier);
                break;
            case "Guid":
                this.nodeId=NodeId.get(IdType.Guid,nameSpaceIndex,identifier);
                break;
            case "Numeric":
                this.nodeId=NodeId.get(IdType.Numeric,nameSpaceIndex,identifier);
                break;
            case "Opaque":
                this.nodeId=NodeId.get(IdType.Opaque,nameSpaceIndex,identifier);
                break;
            default:
                LOG.info("not support nodeId type "+nameSpaceIndex+":"+identifier+":"+idType);
        }
    }


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

    public MessageSecurityMode getMessageSecurityMode() {
        return messageSecurityMode;
    }

    public void setMessageSecurityMode(String messageSecurityMode) {
        //None; Sign; SignAndEncrypt;
        switch (messageSecurityMode) {
            case "Sign":
                this.messageSecurityMode=MessageSecurityMode.Sign;
                break;
            case "SignAndEncrypt":
                this.messageSecurityMode=MessageSecurityMode.SignAndEncrypt;
                break;
            case "None":
                this.messageSecurityMode=MessageSecurityMode.None;
                break;
            default:
               LOG.info("not support MessageSecurityMode "+messageSecurityMode);
        }
    }

    public String getOpcServerUrl() {
        return opcServerUrl;
    }

    public void setOpcServerUrl(String opcServerUrl) {
        this.opcServerUrl = opcServerUrl;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(String securityPolicy) {
        switch (securityPolicy) {
            case "BASIC128RSA15":
                this.securityPolicy=SecurityPolicy.BASIC128RSA15;
                break;
            case "BASIC256":
                this.securityPolicy=SecurityPolicy.BASIC256;
                break;
            case "BASIC256SHA256":
                this.securityPolicy=SecurityPolicy.BASIC256SHA256;
                break;
            case "AES128_SHA256_RSAOAEP":
                this.securityPolicy=SecurityPolicy.AES128_SHA256_RSAOAEP;
                break;
            case "AES256_SHA256_RSAPSS":
                this.securityPolicy=SecurityPolicy.AES256_SHA256_RSAPSS;
                break;
            case  "NONE":
                this.securityPolicy=SecurityPolicy.NONE;
                break;
            default:
                LOG.info("not support Security "+securityPolicy);
        }
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
