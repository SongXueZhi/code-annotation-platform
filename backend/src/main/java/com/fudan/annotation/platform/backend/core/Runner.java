package com.fudan.annotation.platform.backend.core;

import java.io.File;

/**
 * description: revision run
 *
 * @author Richy
 * create: 2022-03-07 10:36
 **/
public class Runner {
    protected File revDir;
    protected String testCase;
    protected String runResult;
    protected String runTestResult;

    public Runner(File revDir, String testCase) {
        this.revDir = revDir;
        this.testCase = testCase;
        this.runResult = getRunCode();
        this.runResult = getRunCode();
    }

    public String getRunCode() {
        this.run();
        return this.runTestResult;
    }

    private void run() {
        // execute the test
        String buildCommand = "mvn compile";
        String testCommand = "mvn test -Dtest=" + this.testCase + " " + "-Dmaven.test.failure.ignore=true";
        try {
            runResult = new Executor().setDirectory(this.revDir).exec(buildCommand);
            runTestResult = new Executor().setDirectory(this.revDir).exec(testCommand, 5);
            System.out.println(runTestResult);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}