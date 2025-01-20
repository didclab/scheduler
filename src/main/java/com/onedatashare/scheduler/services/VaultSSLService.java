package com.onedatashare.scheduler.services;

import com.hazelcast.nio.ssl.SSLContextFactory;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultPkiOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.CertificateBundle;
import org.springframework.vault.support.VaultCertificateRequest;
import org.springframework.vault.support.VaultCertificateResponse;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class VaultSSLService implements SSLContextFactory {

    private final VaultPkiOperations vaultPkiOperations;
    private final String vaultTransferServiceRole = "onedatashare-dot-org";
    @Getter
    protected final Path storePath;
    Logger logger = LoggerFactory.getLogger(VaultSSLService.class);
    private String keyStorePassword;
    @Getter
    public Duration storeDuration;
    private ScheduledExecutorService scheduler;
    private SSLContext sslContext;


    public VaultSSLService(VaultTemplate vaultTemplate, Environment env) {
        this.vaultPkiOperations = vaultTemplate.opsForPki();
        this.storePath = Paths.get(System.getProperty("user.home"), "onedatashare", "jobscheduler", "store", "jobscheduler.keystore.p12");
        storeDuration = Duration.ofHours(2160);
        this.keyStorePassword = env.getProperty("hz.keystore.password", "changeit");
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                refreshSslCerts();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }, 0, Duration.ofDays(1).toSeconds(), java.util.concurrent.TimeUnit.SECONDS);
    }

    /***
     * Run this faster than by the time the certificates expire.
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public void refreshSslCerts() throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException {
        //Check if we have the trust and keystore locally
        logger.info("Refreshing Certificates");
        KeyStore keyStore = this.readInKeyStore();
        boolean hasValidCerts = this.checkIfCertsAreStillValid(keyStore);
        logger.info("Certs are valid: {}", hasValidCerts);
        if (keyStore == null || !hasValidCerts) {
            VaultCertificateRequest request = VaultCertificateRequest.builder()
                    .ttl(this.storeDuration)
                    .commonName("*" + ".onedatashare.org")
                    .build();
            VaultCertificateResponse certificateResponse = this.vaultPkiOperations.issueCertificate(this.vaultTransferServiceRole, request);
            CertificateBundle bundle = certificateResponse.getData();
            keyStore = bundle.createKeyStore("transfer-scheduler-store", true, this.keyStorePassword.toCharArray());
            this.persistStore(keyStore);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, this.keyStorePassword.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), null, null);
        this.sslContext = sslContext;
    }

    private KeyStore readInKeyStore() throws KeyStoreException {
        if (Files.exists(storePath)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream inputStream = Files.newInputStream(storePath, StandardOpenOption.READ)) {
                keyStore.load(inputStream, this.keyStorePassword.toCharArray());
                return keyStore;
            } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
                return null;
            }
        }
        return null;
    }

    private boolean checkIfCertsAreStillValid(KeyStore keyStore) throws KeyStoreException {
        if (keyStore == null) return false;
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            try {
                certificate.checkValidity();
            } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                return false;
            }
        }
        return true;
    }

    private void persistStore(KeyStore store) throws IOException, KeyStoreException, NoSuchAlgorithmException {
        if (!Files.exists(storePath)) {
            Files.createDirectories(storePath.getParent());
            Files.createFile(storePath);
        }
        try (OutputStream outputStream = Files.newOutputStream(storePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            logger.debug("Persisting the KeyStore to {}", storePath);
            try {
                store.store(outputStream, this.keyStorePassword.toCharArray());
            } catch (CertificateException e) {
            }
        }
    }

    @Override
    public void init(Properties properties) throws Exception {
    }

    @Override
    public SSLContext getSSLContext() {
        if(this.sslContext == null){
            try {
                this.refreshSslCerts();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | KeyManagementException |
                     UnrecoverableKeyException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return this.sslContext;
    }
}
