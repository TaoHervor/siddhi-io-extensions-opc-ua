package io.siddhi.extension.io.opc.source;

import io.siddhi.core.stream.input.source.SourceEventListener;
import net.minidev.json.JSONObject;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.*;
import org.opcfoundation.ua.transport.security.KeyPair;

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
import static org.opcfoundation.ua.utils.EndpointUtil.selectByProtocol;

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

    public void setOpcSourceState(OpcSource.OpcSourceState opcSourceState) {
        this.opcSourceState = opcSourceState;
        for (OpcReadThread opcReadThread : opcReadThreadList) {
            opcReadThread.setOpcSourceState(opcSourceState);
        }
    }


    OpcClientGroup(String opcAppName,String certPath,String privPath,ExecutorService executorService, SourceEventListener sourceEventListener, String opcServerUrl) throws ServiceResultException, IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, InvalidParameterSpecException, InvalidKeySpecException, IllegalBlockSizeException {
        KeyPair clientApplicationInstanceCertificate = null;
        if (!certPath.isEmpty() && !privPath.isEmpty()) {
           clientApplicationInstanceCertificate = getOPCCertByFile(certPath,privPath);
        } else if (!opcAppName.isEmpty()){
           clientApplicationInstanceCertificate = getOPCCertByAppName(opcAppName);
        }else {
           LOG.info("required opcAppName or certFile");
        }
        this.client = Client.createClientApplication(clientApplicationInstanceCertificate);
        this.executorService=executorService;
        // Discover server's endpoints, and choose one
        EndpointDescription[]  endpoints = client.discoverEndpoints(opcServerUrl);
        // Filter out all but opc.tcp protocol endpoints
        if (opcServerUrl.startsWith("opc.tcp")) {
            endpoints = selectByProtocol(endpoints, "opc.tcp");
        }

        if (endpoints.length !=0) {
           for (int i = 0; i < endpoints.length; i++) {
                 OpcReadThread opcReadThread=new OpcReadThread(sourceEventListener,client,endpoints[i]);
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
