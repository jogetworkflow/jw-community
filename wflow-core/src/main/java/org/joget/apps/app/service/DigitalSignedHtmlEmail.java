package org.joget.apps.app.service;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;

public class DigitalSignedHtmlEmail extends HtmlEmail {
    protected PrivateKey privateKey;
    protected X509Certificate issuerCertificate;
    protected X509Certificate certificate;
    
    public DigitalSignedHtmlEmail(String p12Path, String storePassword, String issuer, String formAddress) {
        super();
        readCertificateDetailsP12(p12Path, storePassword, issuer, formAddress);
    }
    
    @Override
    public void buildMimeMessage() throws EmailException
    {
        super.buildMimeMessage();
        
        if (privateKey != null && certificate != null && issuerCertificate != null) {
            signMessage();
        }
    }
    
    public void signMessage() {
        try {
            // Add the list of certs to the generator
            List<X509Certificate> certList = new ArrayList<X509Certificate>();
            certList.add(certificate);
            certList.add(issuerCertificate);

            // Create the SMIMESignedGenerator
            SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
            capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
            capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
            capabilities.addCapability(SMIMECapability.dES_CBC);
            capabilities.addCapability(SMIMECapability.aES256_CBC);

            ASN1EncodableVector attributes = new ASN1EncodableVector();
            attributes.add(new SMIMECapabilitiesAttribute(capabilities));

            IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(new X500Name(certificate.getIssuerDN().getName()),
            certificate.getSerialNumber());
            attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));
            
            SMIMESignedGenerator signer = new SMIMESignedGenerator();

            signer.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
            .setProvider(new BouncyCastleProvider())
            .setSignedAttributeGenerator(new AttributeTable(attributes))
            .build("SHA1withRSA", privateKey, certificate));

            JcaCertStore bcerts = new JcaCertStore(certList);
            signer.addCertificates(bcerts);
            
            MimeBodyPart m = new MimeBodyPart();
            m.setContent((MimeMultipart) message.getContent());
           
            MimeMultipart signedMultipart = signer.generate(m);
            message.setContent(signedMultipart, signedMultipart.getContentType());
            message.saveChanges();
        } catch (Exception e) {
            LogUtil.error(DigitalSignedHtmlEmail.class.getName(), e, "");
        }
    }
    
    public final void readCertificateDetailsP12(String p12Path, String storePassword, String issuer, String formAddress) {
        try {
            // validate input
            String normalizedJksPath = Normalizer.normalize(p12Path, Normalizer.Form.NFKC);
            if (normalizedJksPath.contains("../") || normalizedJksPath.contains("..\\")) {
                throw new SecurityException("Invalid filename " + normalizedJksPath);
            }
            
            String path = SetupManager.getBaseDirectory();
            p12Path = path + p12Path;
            
            KeyStore keyStore = KeyStore.getInstance("pkcs12");

            // Provide location of Java Keystore and password for access
            keyStore.load(new FileInputStream(p12Path), storePassword.toCharArray());
            
            if (keyStore.containsAlias(issuer)) {
                Certificate[] chain = keyStore.getCertificateChain(issuer);
                if (chain != null && chain.length > 0) {
                    issuerCertificate = (X509Certificate) (X509Certificate) chain[0];
                } else {
                    issuerCertificate = (X509Certificate) keyStore.getCertificate(issuer);
                }
            }
            
            if (keyStore.containsAlias(formAddress)) {
                Certificate[] chain = keyStore.getCertificateChain(formAddress);
                if (chain != null && chain.length > 0) {
                    certificate = (X509Certificate) chain[0];
                }

                KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(formAddress,
                    new KeyStore.PasswordProtection(storePassword.toCharArray()));
                if (pkEntry != null) {
                    privateKey = pkEntry.getPrivateKey();
                }
            }
        } catch (Exception e) {
            LogUtil.error(DigitalSignedHtmlEmail.class.getName(), e, p12Path);
        }
    }
}
