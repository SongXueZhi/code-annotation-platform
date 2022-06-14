package com.fudan.annotation.platform.backend.service.impl;

import com.fudan.annotation.platform.backend.config.Configs;
import com.fudan.annotation.platform.backend.core.Executor;
import com.fudan.annotation.platform.backend.core.Migrator;
import com.fudan.annotation.platform.backend.core.SourceCodeManager;
import com.fudan.annotation.platform.backend.dao.ProjectMapper;
import com.fudan.annotation.platform.backend.dao.RegressionMapper;
import com.fudan.annotation.platform.backend.entity.*;
import com.fudan.annotation.platform.backend.service.RegressionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * description:
 *
 * @author Richy
 * create: 2021-12-10 16:05
 **/

@Service
@Slf4j
public class RegressionServiceImpl implements RegressionService {
    private final static String NULL = "/dev/null";
    private final static String GITHUB_URL = "https://github.com";
    private final static String COMMIT = "commit";
    private RegressionMapper regressionMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SourceCodeManager sourceCodeManager;
    @Autowired
    private Migrator migrator;

    @Override
    public List<Regression> getRegressions(String regressionUuid, Integer regressionStatus, String projectName) {
        return regressionMapper.selectRegression(regressionUuid, regressionStatus, projectName);
    }

    @Override
    public void insertRegression(Regression regressionInfo) {
        if (regressionInfo.getProjectFullName() == null) {
            throw new RuntimeException("param loss");
        }
        if (regressionInfo.getRegressionStatus() == null) {
            regressionInfo.setRegressionStatus(0);
        }
        String RegUuid = UUID.randomUUID().toString() + '_' + regressionInfo.getBfc().substring(0, 8);
        regressionInfo.setRegressionUuid(RegUuid);
        regressionMapper.insert(regressionInfo);
    }

    @Override
    public void deleteRegression(String regressionUuid) {
        regressionMapper.deleteByregressionId(regressionUuid);
    }

    @Override
    public void resetStatus(String regressionUuid, Integer regressionStatus) {
        regressionMapper.updateRegressionStatus(regressionUuid, regressionStatus);
    }

    @Override
    public void addProjectUuid() {
        List<Regression> regressionList = regressionMapper.getRegression();
        for (Regression regression : regressionList) {
            String[] name = regression.getProjectFullName().split("/");
            String organization = name[0];
            String projectName = name[1];
            String projectUuid = projectMapper.getProjectUuid(organization, projectName);
            regressionMapper.setProjectUuid(regression.getRegressionUuid(), projectUuid);
        }
    }

    @Override
    public void addRegressionUuid() {
        List<Regression> regressionList = regressionMapper.getRegression();
        for (Regression regression : regressionList) {
            if (regression.getRegressionUuid() == null) {
                String RegUuid = UUID.randomUUID().toString() + '_' + regression.getBfc().substring(0, 8);
                regressionMapper.setRegressionUuid(regression.getId(), RegUuid);
            }
        }
    }

    @Override
    public RegressionDetail getChangedFiles(String regressionUuid, String userToken) throws Exception {
        Regression regression = regressionMapper.getRegressionInfo(regressionUuid);
        //get projectFile
        File projectFile = sourceCodeManager.getMetaProjectDir(regression.getProjectUuid());

        //get changed files: bic/bfc
        List<ChangedFile> bfcFiles = migrator.getChangedFiles(projectFile, regression.getBuggy(), regression.getBfc());
        List<ChangedFile> bicFiles = migrator.getChangedFiles(projectFile, regression.getBic(), regression.getWork());
        String testCase =  regression.getTestcase().split(";")[0];

        String testCasePath = "NULL";
        boolean hasTest = modifyCorrelationDetect(bfcFiles, bicFiles,testCase);
        if (!hasTest){
            try {
                testCasePath =sourceCodeManager.getTestCasePath(userToken,regressionUuid,"bfc",testCase);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //set regression details
        RegressionDetail regressionDetail = new RegressionDetail();
        regressionDetail.setTestFilePath(testCasePath);

        if (!testCasePath.equals("NULL")){
            String fileName = testCasePath.substring(testCasePath.lastIndexOf("/")+1);
            ChangedFile bfcFile = new ChangedFile();
            bfcFile.setFilename(fileName);
            bfcFile.setNewPath(testCasePath);
            bfcFile.setOldPath(testCasePath);
            bfcFile.setType(ChangedFile.Type.TEST_SUITE);
            bfcFiles.add(bfcFile);

            ChangedFile bicFile = new ChangedFile();
            bicFile.setFilename(fileName);
            bicFile.setNewPath(testCasePath);
            bicFile.setOldPath(testCasePath);
            bicFile.setType(ChangedFile.Type.TEST_SUITE);
            bicFiles.add(bicFile);
        }
        regressionDetail.setBfcURL(String.join("/",GITHUB_URL,regression.getProjectFullName(),COMMIT,
                regression.getBfc()));
        regressionDetail.setBicURL(String.join("/",GITHUB_URL,regression.getProjectFullName(),COMMIT,
                regression.getBic()));
        regressionDetail.setRegressionUuid(regressionUuid);
        regressionDetail.setProjectFullName(regression.getProjectFullName());
        regressionDetail.setBfc(regression.getBfc());
        regressionDetail.setBic(regression.getBic());
        regressionDetail.setBfcChangedFiles(bfcFiles);
        regressionDetail.setBicChangedFiles(bicFiles);
        regressionDetail.setTestCaseName(regression.getTestcase().split(";")[0].split("#")[1]);
        return regressionDetail;
    }

    private boolean modifyCorrelationDetect(List<ChangedFile> bfcFiles, List<ChangedFile> bicFiles,
                                            String testCaseName) {
        boolean result = false;
        testCaseName = testCaseName.substring(0, testCaseName.indexOf("#")).replace(".", "/") + ".java";
        String finalTestCaseName = testCaseName;

        for (ChangedFile changedFile : bfcFiles) {
            if (changedFile.getNewPath().endsWith(finalTestCaseName)) {
                changedFile.setType(ChangedFile.Type.TEST_SUITE);
                result = true;
            }
            for (ChangedFile bicFile : bicFiles) {
                if (bicFile.getMatch() == 1) {
                    continue;
                }
                if (changedFile.getNewPath().equals(bicFile.getNewPath())) {
                    changedFile.setMatch(1);
                    bicFile.setMatch(1);
                    break;
                }
                changedFile.setMatch(0);
                bicFile.setMatch(0);
            }
        }
        return result;
    }

    @Override
    public void checkoutByUser(String regressionUuid, String userToken) {
        Regression regression = regressionMapper.getRegressionInfo(regressionUuid);

        //get projectFile
        File projectFile = sourceCodeManager.getMetaProjectDir(regression.getProjectUuid());
        checkoutBugCode(regressionUuid, projectFile, userToken);
    }

    @Override
    public CodeDetails getFilesCode(String regressionUuid, String userToken, String filename, String oldPath,
                                    String newPath, String revisionFlag) {
        CodeDetails codeDetails = new CodeDetails();
        codeDetails.setRegressionUuid(regressionUuid);
        String oldCode = "";
        String newCode = "";

        if (revisionFlag.equals("bfc")) {
            if (!oldPath.equals(NULL)) {
                File buggyFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "buggy", oldPath);
                oldCode = sourceCodeManager.getRevisionCode(buggyFile);
            }
            if (!newPath.equals(NULL)) {
                File bfcFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "bfc", newPath);
                newCode = sourceCodeManager.getRevisionCode(bfcFile);
            }
        }

        if (revisionFlag.equals("bic")) {
            if (!oldPath.equals(NULL)) {
                File workFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "work", oldPath);
                oldCode = sourceCodeManager.getRevisionCode(workFile);
            }
            if (!newPath.equals(NULL)) {
                File bicFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "bic", newPath);
                newCode = sourceCodeManager.getRevisionCode(bicFile);
            }
        }
        codeDetails.setOldCode(oldCode);
        codeDetails.setNewCode(newCode);
        return codeDetails;
    }

    public void checkoutBugCode(String regressionUuid, File projectFile, String userToken) {

        File bugDir =
                new File(SourceCodeManager.cacheProjectsDirPath + File.separator + userToken + File.separator + regressionUuid);
        if (bugDir.exists()) {
            return;
        }
        Regression regression = regressionMapper.getRegressionInfo(regressionUuid);

        List<Revision> targetCodeVersions = new ArrayList<>(4);
        Revision rfc = new Revision("bfc", regression.getBfc());
        targetCodeVersions.add(rfc);

        targetCodeVersions.add(new Revision("buggy", regression.getBuggy()));
        targetCodeVersions.add(new Revision("bic", regression.getBic()));
        targetCodeVersions.add(new Revision("work", regression.getWork()));

        targetCodeVersions.forEach(revision -> {
            revision.setLocalCodeDir(sourceCodeManager.checkout(revision, projectFile, regressionUuid, userToken));
        });

        targetCodeVersions.remove(0);
        migrator.migrateTestAndDependency(rfc, targetCodeVersions, regression.getTestcase());
    }

    @Override
    public String runTest(String regressionUuid, String userToken, String revisionFlag) {
        Regression regression = regressionMapper.getRegressionInfo(regressionUuid);
        String testCase = regression.getTestcase().split(";")[0];
        File codeDir = sourceCodeManager.getCodeDir(regressionUuid, userToken, revisionFlag);

        String logFileName = UUID.randomUUID() + "_" + Configs.RUNTIME_LOG_FILE_NAME;
        String logPath = codeDir.getAbsolutePath() + File.separator + logFileName;
        File logFile = new File(logPath);
        logFile.deleteOnExit();

        new Thread(() -> {
            int state =
                    new Executor().setDirectory(codeDir).exec("mvn test -Dtest=" + testCase + " >> " + logFileName, 1);
            try {
                String endFlag = "REGMINER-TEST-END";
                if (state < 0) {
                    endFlag = "TIME OUT ERROR" + "\n" + endFlag;
                }
                FileUtils.writeStringToFile(logFile, endFlag, "UTF-8", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return logPath;
    }

    @Override
    public String readRuntimeResult(String filaPath) throws IOException {
        return FileUtils.readFileToString(new File(filaPath), "UTF-8");
    }

    @Autowired
    public void setRegressionMapper(RegressionMapper regressionMapper) {
        this.regressionMapper = regressionMapper;
    }

}