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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
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
    private static String NULL = "/dev/null";
    private RegressionMapper regressionMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SourceCodeManager sourceCodeManager;
    @Autowired
    private Migrator migrator;

    @Override
    public List<Regression> getRegressions(String regressionUuid, Integer regressionStatus) {
        return regressionMapper.selectRegression(regressionUuid, regressionStatus);
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
    public RegressionDetail getChangedFiles(String regressionUuid) {
        Regression regression = regressionMapper.getRegressionInfo(regressionUuid);
        //get projectFile
        File projectFile = sourceCodeManager.getMetaProjectDir(regression.getProjectUuid());

        //get changed files: bic/bfc
        List<ChangedFile> bfcFiles = migrator.getChangedFiles(projectFile, regression.getBuggy(), regression.getBfc());
        List<ChangedFile> bicFiles = migrator.getChangedFiles(projectFile, regression.getBic(), regression.getWork());

        //set regression details
        RegressionDetail regressionDetail = new RegressionDetail();
        regressionDetail.setRegressionUuid(regressionUuid);
        regressionDetail.setProjectFullName(regression.getProjectFullName());
        regressionDetail.setBfc(regression.getBfc());
        regressionDetail.setBic(regression.getBic());
        regressionDetail.setBfcChangedFiles(bfcFiles);
        regressionDetail.setBicChangedFiles(bicFiles);
        return regressionDetail;
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
                File bfcFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "bfc", oldPath);
                oldCode = sourceCodeManager.getRevisionCode(bfcFile);
            }
            if (!newPath.equals(NULL)) {
                File buggyFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "buggy", newPath);
                newCode = sourceCodeManager.getRevisionCode(buggyFile);
            }
        }

        if (revisionFlag.equals("bic")) {
            if (!oldPath.equals(NULL)) {
                File workFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "work", newPath);
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

    @Async
    public void checkoutBugCode(String regressionUuid, File projectFile, String userToken) {
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

        String logPath =  codeDir.getAbsolutePath() + File.separator + Configs.RUNTIME_LOG_FILE_NAME;
        File logFile = new File(logPath);
        logFile.deleteOnExit();

        new Thread(() -> {
            new Executor().setDirectory(codeDir).exec("mvn test -Dtest=" + testCase + " >> " + Configs.RUNTIME_LOG_FILE_NAME);
            synchronized (logPath){
                try {
                    FileUtils.writeStringToFile(logFile,"REGMINER-TEST-END","UTF-8",true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return codeDir.getAbsolutePath() + File.separator + Configs.RUNTIME_LOG_FILE_NAME;
    }

    public String readRuntimeResult(String filaPath) throws IOException {
        return FileUtils.readFileToString(new File(filaPath), "UTF-8");
    }

    @Autowired
    public void setRegressionMapper(RegressionMapper regressionMapper) {
        this.regressionMapper = regressionMapper;
    }

}