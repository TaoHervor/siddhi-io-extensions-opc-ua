package io.siddhi.extension.io.opc.exmaple;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.output.StreamCallback;
import org.apache.log4j.Logger;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.exmaple
 * @date 2020/4/10 10:22
 * @Copyright © 2019-2020  QuickSilverDB
 */
public class SourceExample1 {

    private static final Logger log = Logger.getLogger(SourceExample1.class);


    public static void main(String[] args) {

        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream outputStream (id string,uri string,sourceTimestamp string,serverTimestamp string,value string,statusCode string," +
                        "sourcePicoseconds int, serverPicoseconds int); " +
                        ""+
                        "@info(name='query1') " +
                        "@source(type='opc-ua', opc.server.url='opc.tcp://DESKTOP-4S28773:53530/OPCUA/SimulationServer', opc.app.name='test', "
                        + "server.cert.path='', server.priv.path='', client.timeout='60000',max.message.length='41200',"
                        + "message.security.mode='Sign',authentication.mode='2',security.policy='BASIC256',"
                        + "user.name='user',user.password='12345678',opc.node.id='5:Counter1:String')" +
                        "Define stream inputStream (id string,uri string,sourceTimestamp string,serverTimestamp string,value string,statusCode string," +
                        "sourcePicoseconds int,serverPicoseconds int); " +
                        "from inputStream select id,uri,sourceTimestamp,serverTimestamp,value,statusCode,sourcePicoseconds,serverPicoseconds" +
                        " insert into outputStream;");


        siddhiAppRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    System.out.println(event);
                }
            }
        });

        siddhiAppRuntime.start();

        //Shutdown runtime
        //siddhiAppRuntime.shutdown();

        //Shutdown Siddhi Manager
        // siddhiManager.shutdown();

    }


}
