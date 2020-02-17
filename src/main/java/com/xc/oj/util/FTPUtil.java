package com.xc.oj.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@ConfigurationProperties(prefix = "spring.ftp")
public class FTPUtil {
    private static String host;
    private static int port;
    private static String username;
    private static String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private static FTPClient connect() throws IOException {
        FTPClient ftp=new FTPClient();
        ftp.connect(host,port);
        ftp.login(username,password);
        if(FTPReply.isPositiveCompletion(ftp.getReplyCode()))
            return ftp;
        ftp.disconnect();
        return null;
    }
    public static boolean upload(File file) throws IOException {
        FTPClient ftp=connect();
        if(ftp==null)
            return false;
        boolean ret=ftp.storeFile(file.getName(),new FileInputStream(file));
        if(ftp.isConnected())
            ftp.disconnect();
        return ret;
    }
    public static boolean download(File file) throws IOException {
        FTPClient ftp=connect();
        if(ftp==null)
            return false;
        OutputStream os=new FileOutputStream(file);
        boolean ret=ftp.retrieveFile(file.getName(),os);
        os.close();
        if(ftp.isConnected())
            ftp.disconnect();
        return ret;
    }
}
