package io.siddhi.extension.io.opc.source;

import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.extension.io.opc.utils.OpcConfig;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.application.Application;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.security.CertificateValidator;
import org.opcfoundation.ua.transport.security.HttpsSecurityPolicy;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static io.siddhi.extension.io.opc.utils.GenerateOpcCert.*;
import static org.opcfoundation.ua.utils.EndpointUtil.*;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.source
 * @date 2020/4/7 9:24
 * @Copyright © 2019-2020  QuickSilverDB
 */
public class OpcClientGroup {

    private static final Logger LOG = Logger.getLogger(OpcClientGroup.class);
    private Client client;
    private ExecutorService executorService;
    private OpcSource.OpcSourceState opcSourceState;
    private List<OpcReadThread> opcReadThreadList = new ArrayList<OpcReadThread>();
    private static final int CLIENT_KEY_SIZE=1024;

    public void setOpcSourceState(OpcSource.OpcSourceState opcSourceState) {
        this.opcSourceState = opcSourceState;
        for (OpcReadThread opcReadThread : opcReadThreadList) {
            opcReadThread.setOpcSourceState(opcSourceState);
        }
    }


    OpcClientGroup(OpcConfig opcConfig,ExecutorService executorService, SourceEventListener sourceEventListener)
            throws ServiceResultException, IOException, CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException,
            BadPaddingException, InvalidParameterSpecException, InvalidKeySpecException,
            IllegalBlockSizeException {

        this.executorService = executorService;
        KeyPair clientApplicationInstanceCertificate = null;
        EndpointDescription[] endpoints = null;

        String certPath = opcConfig.getCertPath();
        String privPath = opcConfig.getPrivPath();
        String opcAppName = opcConfig.getOpcAppName();
        String clientTimeout = opcConfig.getClientTimeout();
        String maxMessageLength = opcConfig.getMaxMessageLength();
        String opcServerUrl = opcConfig.getOpcServerUrl();
        MessageSecurityMode messageSecurityMode = opcConfig.getMessageSecurityMode();
        SecurityPolicy securityPolicy = opcConfig.getSecurityPolicy();

        if (messageSecurityMode != null && !messageSecurityMode.equals(MessageSecurityMode.None)) {
            //Certificate
            if (!certPath.isEmpty() && !privPath.isEmpty()) {
                clientApplicationInstanceCertificate = getOPCCertByFile(certPath, privPath);
            } else if (!opcAppName.isEmpty()) {
                clientApplicationInstanceCertificate = getOPCCertByAppName(opcAppName);
            } else {
                LOG.info("required opcAppName or certFile");
            }
            //Create Client
            this.client = Client.createClientApplication(clientApplicationInstanceCertificate);

            //Configure Client
            if (clientTimeout.isEmpty()) {
                client.setTimeout(120000);
            } else {
                client.setTimeout(Integer.parseInt(clientTimeout));
            }
            if (maxMessageLength.isEmpty()) {
                client.setMaxMessageSize(UnsignedShort.MAX_VALUE.intValue() * 64);
            } else {
                client.setMaxMessageSize(Integer.parseInt(maxMessageLength));
            }
            if (opcServerUrl.startsWith("opc.tcp")) {
                endpoints = client.discoverEndpoints(opcServerUrl);
                endpoints = selectByProtocol(endpoints, "opc.tcp");
                endpoints = selectByMessageSecurityMode(endpoints, messageSecurityMode);
            if (securityPolicy != null) {
                endpoints = selectBySecurityPolicy(endpoints, securityPolicy);
            }
            endpoints = sortBySecurityLevel(endpoints);
         } else if (opcServerUrl.startsWith("opc.https")) {
          /*  KeyPair httpsCertificate = getHttpsCert(opcAppName);
            client.getApplication().getHttpsSettings().setKeyPair(httpsCertificate);
            client.getApplication().getHttpsSettings().setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.getApplication().getHttpsSettings().setHttpsSecurityPolicies(HttpsSecurityPolicy.ALL_104);
            client.getApplication().getHttpsSettings().setCertificateValidator(CertificateValidator.ALLOW_ALL);
            endpoints = client.discoverEndpoints(opcServerUrl);
            endpoints = selectByProtocol(endpoints, "opc.https");*/
            }
        }
        else {
            Application clientApplication = new Application();
            client = new Client(clientApplication);
            endpoints = client.discoverEndpoints(opcServerUrl);
            endpoints = selectByProtocol(endpoints, "opc.tcp");
            endpoints = selectByMessageSecurityMode(endpoints, messageSecurityMode);
        }
        if (endpoints.length == 0) {
            throw new IllegalArgumentException("No suitable endpoint found from " + opcServerUrl);
        } else {
            for (int i = 0; i < endpoints.length; i++) {
                OpcReadThread opcReadThread=new OpcReadThread(sourceEventListener,client,opcConfig,endpoints[i]);
                opcReadThreadList.add(opcReadThread);
            }
        }

    }

    void pause() {
        opcReadThreadList.forEach(OpcReadThread::pause);
    }

    void resume() {
        opcReadThreadList.forEach(OpcReadThread::resume);
    }

    void restoreState() {
     //   opcReadThreadList.forEach(OpcReadThread::resume);
    }

    void shutdown() {
        opcReadThreadList.forEach(OpcReadThread::shutdown);
    }

    void run() {
        try {
            for (OpcReadThread opcReadThread : opcReadThreadList) {
                executorService.submit(opcReadThread);
            }
        } catch (Throwable t) {
            LOG.error("Error while creating readThread" , t);
        }
    }

}
