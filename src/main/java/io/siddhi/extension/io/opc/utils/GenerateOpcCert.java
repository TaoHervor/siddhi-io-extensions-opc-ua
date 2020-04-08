package io.siddhi.extension.io.opc.utils;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.utils.CertificateUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

/**
 * @author Hervor
 * @version V1.0
 * @Package io.siddhi.extension.io.opc.utils
 * @date 2020/4/7 8:48
 * @Copyright © 2019-2020  QuickSilverDB
 */
public class GenerateOpcCert {

    private static String PRIVKEY_PASSWORD = "Opc.Ua";

    public void setPRIVKEY_PASSWORD(String PRIVKEY_PASSWORD) {
        this.PRIVKEY_PASSWORD = PRIVKEY_PASSWORD;
    }

    public static KeyPair getOPCCertByAppName(String applicationName) throws ServiceResultException {
        File certFile = new File(applicationName + ".der");
        File privKeyFile =  new File(applicationName+ ".pem");
        try {
            Cert myServerCertificate = Cert.load(certFile);
            PrivKey myServerPrivateKey = PrivKey.load(privKeyFile, PRIVKEY_PASSWORD);
            return new KeyPair(myServerCertificate, myServerPrivateKey);
        } catch (CertificateException e) {
            throw new ServiceResultException( e );
        } catch (IOException e) {
            try {
                String hostName = InetAddress.getLocalHost().getHostName();
                String applicationUri = "urn:"+hostName+":"+applicationName;
                KeyPair keys = CertificateUtils.createApplicationInstanceCertificate(applicationName, null, applicationUri, 3650, hostName);
                keys.getCertificate().save(certFile);
                PrivKey privKeySecure = keys.getPrivateKey();
                privKeySecure.save(privKeyFile, PRIVKEY_PASSWORD);
                return keys;
            } catch (Exception e1) {
                throw new ServiceResultException( e1 );
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceResultException( e );
        } catch (Exception e) {
            throw new ServiceResultException( e );
        }
    }


    public static KeyPair getOPCCertByFile(String certFilePath,String privKeyFilePath) throws IOException, CertificateException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException, InvalidKeyException, InvalidKeySpecException {
        File certFile = new File(certFilePath);
        File privKeyFile =  new File(privKeyFilePath);
        Cert myServerCertificate = Cert.load(certFile);
        PrivKey myServerPrivateKey = PrivKey.load(privKeyFile, PRIVKEY_PASSWORD);
        return new KeyPair(myServerCertificate, myServerPrivateKey);
    }

    public static KeyPair getHttpsCert(String applicationName)
            throws ServiceResultException
    {
        File certFile = new File(applicationName + "_https.der");
        File privKeyFile =  new File(applicationName+ "_https.pem");
        try {
            Cert myCertificate = Cert.load( certFile );
            PrivKey myPrivateKey = PrivKey.load( privKeyFile, PRIVKEY_PASSWORD );
            return new KeyPair(myCertificate, myPrivateKey);
        } catch (CertificateException e) {
            throw new ServiceResultException( e );
        } catch (IOException e) {
            try {
                KeyPair caCert = getCACert(applicationName);
                String hostName = InetAddress.getLocalHost().getHostName();
                String applicationUri = "urn:"+hostName+":"+applicationName;
                KeyPair keys = CertificateUtils.createHttpsCertificate(hostName, applicationUri, 3650, caCert);
                keys.getCertificate().save(certFile);
                keys.getPrivateKey().save(privKeyFile, PRIVKEY_PASSWORD);
                return keys;
            } catch (Exception e1) {
                throw new ServiceResultException( e1 );
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceResultException( e );
        } catch (InvalidKeyException e) {
            throw new ServiceResultException( e );
        } catch (InvalidKeySpecException e) {
            throw new ServiceResultException( e );
        } catch (NoSuchPaddingException e) {
            throw new ServiceResultException( e );
        } catch (InvalidAlgorithmParameterException e) {
            throw new ServiceResultException( e );
        } catch (IllegalBlockSizeException e) {
            throw new ServiceResultException( e );
        } catch (BadPaddingException e) {
            throw new ServiceResultException( e );
        } catch (InvalidParameterSpecException e) {
            throw new ServiceResultException( e );
        }
    }

    public static KeyPair getCACert(String applicationName)
            throws ServiceResultException
    {
        File certFile = new File(applicationName + ".der");
        File privKeyFile =  new File(applicationName+ ".pem");
        try {
            Cert myCertificate = Cert.load( certFile );
            PrivKey myPrivateKey = PrivKey.load( privKeyFile, PRIVKEY_PASSWORD );
            return new KeyPair(myCertificate, myPrivateKey);
        } catch (CertificateException e) {
            throw new ServiceResultException( e );
        } catch (IOException e) {
            try {
                KeyPair keys = CertificateUtils.createIssuerCertificate("SampleCA", 3650, null);
                keys.getCertificate().save(certFile);
                keys.getPrivateKey().save(privKeyFile, PRIVKEY_PASSWORD);
                return keys;
            } catch (Exception e1) {
                throw new ServiceResultException( e1 );
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceResultException( e );
        } catch (InvalidKeyException e) {
            throw new ServiceResultException( e );
        } catch (InvalidKeySpecException e) {
            throw new ServiceResultException( e );
        } catch (NoSuchPaddingException e) {
            throw new ServiceResultException( e );
        } catch (InvalidAlgorithmParameterException e) {
            throw new ServiceResultException( e );
        } catch (IllegalBlockSizeException e) {
            throw new ServiceResultException( e );
        } catch (BadPaddingException e) {
            throw new ServiceResultException( e );
        } catch (InvalidParameterSpecException e) {
            throw new ServiceResultException( e );
        }
    }

}
