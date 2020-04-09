package io.siddhi.extension.io.opc.source;

import io.siddhi.core.event.Event;
import io.siddhi.core.event.stream.StreamEvent;
import io.siddhi.core.event.stream.StreamEventFactory;
import io.siddhi.core.event.stream.converter.StreamEventConverter;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.extension.io.opc.utils.OpcConfig;
import net.minidev.json.JSONObject;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.*;
import org.opcfoundation.ua.transport.ServiceChannel;
import sun.awt.datatransfer.DataTransferer;

import java.rmi.activation.ActivationInstantiator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.opcfoundation.ua.utils.EndpointUtil.selectByProtocol;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.source
 * @date 2020/4/7 9:25
 * @Copyright © 2019-2020  QuickSilverDB
 */
public class OpcReadThread implements Runnable {

    private static final Logger LOG = Logger.getLogger(OpcReadThread.class);
    private OpcSource.OpcSourceState opcSourceState;
    private SourceEventListener sourceEventListener;
    private volatile boolean paused;
    private volatile boolean inactive;
    private Client client;
    private ReentrantLock lock;
    private Condition condition;
    private  EndpointDescription  endpoint;
    private SessionChannel session;
    private OpcConfig opcConfig;

    OpcReadThread(SourceEventListener sourceEventListener,Client client,OpcConfig opcConfig,EndpointDescription endpoint) throws ServiceResultException {
        this.sourceEventListener=sourceEventListener;
        this.endpoint=endpoint;
        this.client=client;
        this.opcConfig=opcConfig;
    }

    void pause() {
        paused=true;
    }

    void resume() {
        restore();
        paused=false;
        try {
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    void restore() {

    }

    void shutdown(){
        lock.lock();
        try {
            session.close();
        } catch (ServiceResultException e) {
            e.printStackTrace();
        }
        session.closeAsync();
        lock.unlock();
        inactive=true;
    }


    public void setOpcSourceState(OpcSource.OpcSourceState opcSourceState) {
        this.opcSourceState = opcSourceState;
    }

    public void run() {
        while (!inactive) {
            if (paused) {
                lock.lock();
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            }
            try {
                Integer authentication=opcConfig.getAuthentication();
                session = client.createSessionChannel(endpoint);
                switch (authentication) {
                    case 1:
                        session.activate(opcConfig.getUserName(),opcConfig.getPassWord());
                        break;
                    default:
                        session.activate();
                }
                // Read current time
                NodeId nodeId = Identifiers.Server_ServerStatus_CurrentTime;
                ReadResponse readResponse = session.Read(
                        null,
                        500.0,
                        TimestampsToReturn.Both,
                        new ReadValueId(nodeId, Attributes.Value, null, null)
                );
                // convert node to JSON
             /*   JSONObject obj = new JSONObject();
                obj.put("Id", nodeId.toString());
                obj.put("Uri", endpoint.getServer().getApplicationUri());

                JSONObject value = new JSONObject();
                value.put("Value", readResponse.getResults()[0].getValue().toString());
                value.put("SourceTimestamp", readResponse.getResults()[0].getSourceTimestamp().toString());
                value.put("ServerTimestamp", readResponse.getResults()[0].getServerTimestamp().toString());

                JSONObject monitoredItem = new JSONObject();
                monitoredItem.put("MonitoredItem", obj);
                monitoredItem.put("ClientHandle", 2);
                monitoredItem.put("Value", value);
                // publish JSON string
                String msgStr = monitoredItem.toString();*/
                System.out.println(readResponse.getResults()[0]);
                Object[] event= new Object[]{nodeId.toString(),endpoint.getServer().getApplicationUri(),
                        readResponse.getResults()[0].getSourceTimestamp().toString(),
                        readResponse.getResults()[0].getServerTimestamp().toString(),
                        readResponse.getResults()[0].getValue().toString()
                };
                String[] transportSyncPropertiesArr = new String[]{};
                sourceEventListener.onEvent(event,transportSyncPropertiesArr);
            } catch (ServiceResultException e) {
                session.closeAsync();
                e.printStackTrace();
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
        }
        }
    }


}
