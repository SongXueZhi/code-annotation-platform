package com.fudan.annotation.platform.backend.core;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * description:
 *
 * @author Richy
 * create: 2022-03-07 10:48
 **/
public class Executor {

    public final static String OS_WINDOWS = "windows";
    public final static String OS_MAC = "mac";
    public final static String OS_UNIX = "unix";
    protected static String OS;

    static {
        OS = System.getProperty("os.name").toLowerCase();
    }

    ProcessBuilder pb = new ProcessBuilder();

    public Executor setDirectory(File file) {
        pb.directory(file);
        return this;
    }

    public String exec(String cmd) {
        try {
            return this.exec(cmd, 0);
        } catch (TimeoutException e) {
            e.printStackTrace(); // should not timeout
            return null;
        }
    }

    public String exec(String cmd, int timeout) throws TimeoutException {
        StringBuilder builder = new StringBuilder();
        Process process = null;
        InputStreamReader inputStr = null;
        BufferedReader bufferReader = null;
        pb.redirectErrorStream(true);
        try {
            if (OS.contains(OS_WINDOWS)) {
                pb.command("cmd.exe", "/c", cmd);
            } else {
                pb.command("bash", "-c", cmd);
            }
            process = pb.start();
            if (timeout > 0) {
                boolean completed = process.waitFor(timeout, TimeUnit.MINUTES);
                if (!completed) {
                    throw new TimeoutException();
                }
            }
            inputStr = new InputStreamReader(process.getInputStream());
            bufferReader = new BufferedReader(inputStr);
            String line;
            while ((line = bufferReader.readLine()) != null) {
                builder.append("\n").append(line);
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
                if (inputStr != null) {
                    IOUtils.close(inputStr);
                }
                if (bufferReader != null) {
                    IOUtils.close(bufferReader);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}