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
import io.siddhi.extension.io.opc.utils.OpcConfig;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

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
                        defaultValue = "null"),
                @Parameter(name="client.timeout",
                        description = "sds",
                        type={DataType.STRING},
                        optional = true,
                        defaultValue = "120000"),
                @Parameter(name="max.message.length",
                        description = "ds",
                        type={DataType.STRING},
                        optional = true,
                        defaultValue = "4194240"),
                @Parameter(name="message.security.mode",
                        description = "ds",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "MessageSecurityMode.None"),
                @Parameter(name="authentication.mode",
                        description = "ds",
                        type = {DataType.STRING},
                        optional = true,
                        defaultValue = "0"),
                @Parameter(name="security.policy",
                        description = "ds",
                        type={DataType.STRING},
                        optional = true,
                        defaultValue ="SecurityPolicy.NONE"),
                @Parameter(name="user.name",
                        description = "ds",
                        type = {DataType.STRING},
                        defaultValue = "0"),
                @Parameter(name="user.password",
                        description = "ds",
                        type={DataType.STRING},
                        defaultValue ="SecurityPolicy.NONE")
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
    public static final String MESSAGE_SECURITY_MODE="message.security.mode";
    public static final String SECURITY_POLICY="security.policy";
    public static final String USER_NAME="user.name";
    public static final String USER_PASSWORD="user.password";
    public static final String AUTHENTICATION_MODE="authentication.mode";
    private static final Logger LOG = Logger.getLogger(OpcSource.class);

    private String opcServerUrl;
    private String opcAppName;
    private String certPath;
    private String privPath;
    private String clientTimeout;
    private String maxMessageLength;
    private String messageSecurityMode;
    private String userName;
    private String password;
    private String authenticationMode;
    private SiddhiAppContext siddhiAppContext;
    private OpcSourceState opcSourceState;
    private SourceEventListener sourceEventListener;
    private OptionHolder optionHolder;
    private OpcClientGroup opcClientGroup;
    private OpcConfig opcConfig;
    private String securityPolicy;

    protected ServiceDeploymentInfo exposeServiceDeploymentInfo() {
        return null;
    }

    public StateFactory<OpcSourceState> init(SourceEventListener sourceEventListener, OptionHolder optionHolder, String[] requestedTransportPropertyNames, ConfigReader configReader, SiddhiAppContext siddhiAppContext) {

        this.siddhiAppContext = siddhiAppContext;
        this.optionHolder = optionHolder;
        this.sourceEventListener = sourceEventListener;
        this.opcConfig=new OpcConfig();

        opcServerUrl = optionHolder.validateAndGetStaticValue(OPC_SERVER_URL);
        opcAppName = optionHolder.validateAndGetStaticValue(OPC_APP_NAME);
        certPath = optionHolder.validateAndGetStaticValue(SERVER_CERT_PATH);
        privPath = optionHolder.validateAndGetStaticValue(SERVER_PRIV_PATH);
        clientTimeout=optionHolder.validateAndGetStaticValue(CLIENT_TIMEOUT);
        maxMessageLength=optionHolder.validateAndGetStaticValue(MAX_MESSAGE_LENGTH);
        messageSecurityMode=optionHolder.validateAndGetStaticValue(MESSAGE_SECURITY_MODE);
        userName=optionHolder.validateAndGetStaticValue(USER_NAME);
        password=optionHolder.validateAndGetStaticValue(USER_PASSWORD);
        authenticationMode=optionHolder.validateAndGetStaticValue(AUTHENTICATION_MODE);
        securityPolicy=optionHolder.validateAndGetStaticValue(SECURITY_POLICY);


        opcConfig.setAuthentication(Integer.parseInt(authenticationMode));
        opcConfig.setCertPath(certPath);
        opcConfig.setMaxMessageLength(maxMessageLength);
        opcConfig.setClientTimeout(clientTimeout);
        opcConfig.setOpcAppName(opcAppName);
        opcConfig.setOpcAppName(privPath);
        opcConfig.setPassWord(password);
        opcConfig.setUserName(userName);
        opcConfig.setOpcServerUrl(opcServerUrl);
        opcConfig.setMessageSecurityMode(messageSecurityMode);
        opcConfig.setSecurityPolicy(securityPolicy);

        return() -> new OpcSourceState();
    }

    public Class[] getOutputEventClasses() {
        return new Class[0];
    }

    public void connect(ConnectionCallback connectionCallback, OpcSourceState state) {
        ExecutorService executorService = siddhiAppContext.getExecutorService();
        try {

            opcClientGroup = new OpcClientGroup(opcConfig,
                    executorService,sourceEventListener);
        } catch (ServiceResultException | IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | InvalidParameterSpecException | InvalidKeySpecException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        this.opcSourceState = state;
        /*if (!opcSourceState.isRestored) {
            opcClientGroup.setOpcSourceState(opcSourceState);
            opcClientGroup.restoreState();
        } else {
            opcClientGroup.setOpcSourceState(opcSourceState);
        }*/
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
