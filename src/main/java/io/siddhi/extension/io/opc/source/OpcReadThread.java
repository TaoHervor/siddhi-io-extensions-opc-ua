package io.siddhi.extension.io.opc.source;

import io.siddhi.core.event.Event;
import io.siddhi.core.event.stream.StreamEvent;
import io.siddhi.core.event.stream.StreamEventFactory;
import io.siddhi.core.event.stream.converter.StreamEventConverter;
import io.siddhi.core.stream.input.source.SourceEventListener;
import net.minidev.json.JSONObject;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.*;
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

    OpcReadThread(SourceEventListener sourceEventListener,Client client,EndpointDescription endpoint) throws ServiceResultException {
        this.sourceEventListener=sourceEventListener;
        this.endpoint=endpoint;
        this.client=client;
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
                session = client.createSessionChannel(endpoint);
                session.activate();
                // Read current time
                NodeId nodeId = Identifiers.Server_ServerStatus_CurrentTime;
                ReadResponse readResponse = session.Read(
                        null,
                        500.0,
                        TimestampsToReturn.Both,
                        new ReadValueId(nodeId, Attributes.Value, null, null)
                );
                // convert node to JSON
                JSONObject obj = new JSONObject();
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
                String msgStr = monitoredItem.toString();
                Object[] event= new Object[]{msgStr,"11111111111"};
                String[] transportSyncPropertiesArr = new String[]{msgStr};
                sourceEventListener.onEvent(event,transportSyncPropertiesArr);
            } catch (ServiceResultException e) {
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
