package io.siddhi.extension.io.opc.source;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiAppContext;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.stream.ServiceDeploymentInfo;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.input.source.SourceEventListener;
import io.siddhi.core.stream.input.source.SourceSyncCallback;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.core.util.transport.OptionHolder;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.common.ServiceResultException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.source
 * @date 2020/4/6 18:54
 * @Copyright © 2019-2020  QuickSilverDB
 */


/*This class implements a opc source to receive events from a opc server*/

@Extension(
        name="opc-ua",
        namespace = "source",
        description = "A Opc source receives events to be processed from a opc-server.",
        parameters = {
                @Parameter(name="opc.server.url",
                        description = "asas",
                        type={DataType.STRING}),
                @Parameter(name="opc.app.name",
                        description = "sasa",
                        type={DataType.STRING}),
                @Parameter(name="server.cert.path",
                        description = "sas",
                        type={DataType.STRING}),
                @Parameter(name="server.priv.path",
                        description = "sasa",
                        type={DataType.STRING}),
                @Parameter(name="opc.cert",
                        description = "sss",
                        type={DataType.OBJECT},
                        optional = true,
                        defaultValue = "null"),
                @Parameter(name="opc.priv",
                        description = "sss",
                        type = {DataType.OBJECT},
                        optional = true,
                        defaultValue = "null")
        },
        examples = {
                @Example(
                        syntax = "@App:name('TestOpc') " +
                                "define stream OutputStream (symbol string, price float, volume long); " +
                                "@info(name = 'query1') " +
                                "@source(" +
                                "type='opc-ua', " +
                                "opc.server.url='opc.tcp://127.0.0.1:8666'," +
                                "opc.app.name='test', " +
                                "server.cert.path=''," +
                                "server.priv.path=''," +
                                "Define stream OutPutStream (symbol string, price float, volume long);" +
                                "from InputStream select symbol, price, volume insert into OutPutStream;",
                        description = "sasas"
                )
        }
)


public class OpcSource extends Source<OpcSource.OpcSourceState>{

    public static final String OPC_SERVER_URL="opc.server.url";
    public static final  String OPC_APP_NAME="opc.app.name";
    public static final String SERVER_CERT_PATH="server.cert.path";
    public static final String SERVER_PRIV_PATH="server.priv.path";
    public static final String CLIENT_TIMEOUT="client.timeout";
    public static final String MAX_MESSAGE_LENGTH="max.message.length";
    public static final String PROTOCOL_TYPE="max.message.length";
    private static final Logger LOG = Logger.getLogger(OpcSource.class);
    private String opcServerUrl;
    private String opcAppName;
    private String certPath;
    private String privPath;
    private int clientTimeout;
    private int maxMessageLength;
    private String protocolType;
    private SiddhiAppContext siddhiAppContext;
    private OpcSourceState opcSourceState;
    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private OpcClientGroup opcClientGroup;

    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    public StateFactory<OpcSourceState> init(SourceEventListener sourceEventListener, OptionHolder optionHolder, String[] requestedTransportPropertyNames, ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        this.siddhiAppContext = siddhiAppContext;
        this.optionHolder = optionHolder;
        this.sourceEventListener = sourceEventListener;
        opcServerUrl = optionHolder.validateAndGetStaticValue(OPC_SERVER_URL);
        opcAppName = optionHolder.validateAndGetStaticValue(OPC_APP_NAME);
        certPath = optionHolder.validateAndGetStaticValue(SERVER_CERT_PATH);
        privPath = optionHolder.validateAndGetStaticValue(SERVER_PRIV_PATH);
        return() -> new OpcSourceState();
    }

    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    public void connect(ConnectionCallback connectionCallback, OpcSourceState state) {
        ExecutorService executorService = siddhiAppContext.getExecutorService();
        try {
            opcClientGroup = new OpcClientGroup(opcAppName,certPath,privPath,executorService,sourceEventListener,opcServerUrl);
        } catch (ServiceResultException | IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | InvalidParameterSpecException | InvalidKeySpecException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        this.opcSourceState = state;
        if (!opcSourceState.isRestored) {
            opcClientGroup.setOpcSourceState(opcSourceState);
            opcClientGroup.restoreState();
        } else {
            opcClientGroup.setOpcSourceState(opcSourceState);
        }
        opcClientGroup.run();
    }


    public void disconnect() {
        this.opcSourceState = null;
        if (opcClientGroup != null) {
            opcClientGroup.setOpcSourceState(null);
            opcClientGroup.shutdown();
        }
        LOG.info("opcClientGroup disconnected!");
    }

    public void destroy() {
        opcClientGroup=null;
    }


    public void pause() {
        if (opcClientGroup != null ) {
            opcClientGroup.pause();
            LOG.info("opcClientGroup paused!");
        }
    }

    public void resume() {
        if (opcClientGroup != null) {
            opcClientGroup.resume();
            if (LOG.isDebugEnabled()) {
                LOG.debug("opcClientGroup resume!");
            }
        }
    }


    /*
     * State class for Opc source
     * */

    public class OpcSourceState extends State {
        private boolean isRestored = false;

        public boolean canDestroy() {
            return false;
        }

        public Map<String, Object> snapshot() {
            Map<String, Object> currentState = new HashMap<>();
            return currentState;
        }

        public void restore(Map<String, Object> state) {
              isRestored=true;
              //topicOffsetMap = (Map<String, Map<Integer, Long>>) state.get(TOPIC_OFFSET_MAP);
            if (opcClientGroup != null) {
                opcClientGroup.restoreState();
            }
        }

    }

}
