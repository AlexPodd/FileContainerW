package com.DBAuthExample.AuthExample.Certificate;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.*;
import java.security.*;

public class GenerateCertificate {
    public String Generate(String name, String randomKey) throws OperatorCreationException, NoSuchAlgorithmException, IOException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");

        SecureRandom random = SecureRandom.getInstanceStrong();
        random.setSeed(randomKey.getBytes());
        keyGen.initialize(4096, random);

        KeyPair keyPair = keyGen.generateKeyPair();
        X500Name subject = new X500Name("CN="+name+", O=My Organization, L=My City, ST=My State, C=My Country");
        PKCS10CertificationRequest csr = generateCSR(subject, keyPair);
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter("src/main/resources/UserCert/"+name+".csr"))) {
            pemWriter.writeObject(csr);
        }
        // for the PKCS8 form
        PemWriter y = new PemWriter (new FileWriter("src/main/resources/UserCert/"+name+".key")); // or whatever
        y.writeObject(new JcaPKCS8Generator(keyPair.getPrivate(), new JceOpenSSLPKCS8EncryptorBuilder(
                PKCS8Generator.DES3_CBC).setPasssword(randomKey.toCharArray()).build() ) );
        // or AES_{128,192,256}_CBC, others will use PBES1 which is deprecated
        y.close(); // or flush to keep underlying writer/stream
        return Sign(name, randomKey);
    }
    private PKCS10CertificationRequest generateCSR(X500Name subject, KeyPair keyPair) throws OperatorCreationException {
        return new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic())
                .build(new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate()));
    }
    public String Sign(String name, String password){
        String csrFile = name+".csr";
        String keyFile = name+".key";
        String caPassword = "password";
        String pkcs12Password = "password";
        String scriptPath = "C:/Users/user/IdeaProjects/SpringSecurity-6-Authorization-and-Authentication/src/main/resources/Certificates/script.sh";

        ProcessBuilder processBuilder = new ProcessBuilder();

        // Установка команды и аргументов
        processBuilder.command("bash", scriptPath, csrFile, keyFile, caPassword, pkcs12Password, password);
        // Установка рабочей директории, если необходимо
        processBuilder.directory(new File("C:/Users/user/IdeaProjects/SpringSecurity-6-Authorization-and-Authentication/src/main/resources/UserCert"));

        try {
            Process process = processBuilder.start();

            // Чтение вывода скрипта
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Чтение ошибок
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with code : " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return name+".p12";
    }

}

