package io.siddhi.extension.io.opc.source;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.stream.output.StreamCallback;
import org.apache.log4j.Logger;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.source
 * @date 2020/4/7 9:26
 * @Copyright © 2019-2020  QuickSilverDB
 */
public class TestCase {

    private static final Logger log = Logger.getLogger(TestCase.class);


    public static void main(String[] args) throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "define stream outputStream (symbol string,aa string); " +
                        ""+
                        "@info(name='query1') " +
                        "@source(type='opc-ua', opc.server.url='opc.tcp://127.0.0.1:8666', opc.app.name='test', "
                        + "server.cert.path='', server.priv.path='')" +
                        "Define stream inputStream (symbol string,aa string);" +
                        "from inputStream select symbol,aa insert into outputStream;");


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
