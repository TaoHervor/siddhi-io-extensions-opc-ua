package io.siddhi.extension.io.opc.source;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.output.StreamCallback;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.source
 * @date 2020/4/7 9:26
 * @Copyright © 2019-2020  QuickSilverDB
 */
public class TestCase {

    private static final Logger log = Logger.getLogger(TestCase.class);


    public static void main(String[] args) {

        System.out.println();
        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream outputStream (Id string,Uri string,SourceTimestamp string,ServerTimestamp string,Value string); " +
                        ""+
                        "@info(name='query1') " +
                        "@source(type='opc-ua', opc.server.url='opc.tcp://127.0.0.1:8666', opc.app.name='test', "
                        + "server.cert.path='', server.priv.path='', client.timeout='60000',max.message.length='41200',"
                        + "message.security.mode='None',authentication.mode='2',security.policy='BASIC256',"
                        + "user.name='user1',user.password='p4ssword')" +
                        "Define stream inputStream (Id string,Uri string,SourceTimestamp string,ServerTimestamp string,Value string);" +
                        "from inputStream select Id,Uri,SourceTimestamp,ServerTimestamp,Value insert into outputStream;");


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
