package com.xc.oj.util;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {
    public static void flow(InputStream in, OutputStream out) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = in.read(buffer, 0, 8192)) != -1)
            out.write(buffer, 0, bytesRead);
    }
    public static boolean unzip(ZipFile zipFile, File dir) throws IOException {
        Enumeration entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry e = (ZipEntry) entries.nextElement();
            File target = new File(dir + File.separator + e.getName());
            target.createNewFile();
            InputStream is = zipFile.getInputStream(e);
            OutputStream os = new FileOutputStream(target);
            flow(is,os);
            os.close();
            is.close();
        }
        zipFile.close();
        return true;
    }
}
