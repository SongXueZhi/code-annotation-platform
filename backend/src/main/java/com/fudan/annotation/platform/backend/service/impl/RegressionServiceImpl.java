package com.fudan.annotation.platform.backend.service.impl;

import com.fudan.annotation.platform.backend.core.Migrator;
import com.fudan.annotation.platform.backend.core.Runner;
import com.fudan.annotation.platform.backend.dao.ProjectMapper;
import com.fudan.annotation.platform.backend.dao.RegressionMapper;
import com.fudan.annotation.platform.backend.entity.*;
import com.fudan.annotation.platform.backend.service.RegressionService;
import com.fudan.annotation.platform.backend.core.SourceCodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
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
    private RegressionMapper regressionMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SourceCodeManager sourceCodeManager;
    @Autowired
    private Migrator migrator;

    private static String NULL = "/dev/null";

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

        RegressionRevision regressionRevision = new RegressionRevision();
        regressionRevision.setRegressionUuid(regression.getRegressionUuid());

        Revision bic = new Revision();
        bic.setRevisionName("bic");
        bic.setCommitID(regression.getBic());
        regressionRevision.setBic(bic);

        Revision bfc = new Revision();
        bfc.setRevisionName("bfc");
        bfc.setCommitID(regression.getBfc());
        regressionRevision.setBfc(bfc);

        Revision buggy = new Revision();
        buggy.setRevisionName("buggy");
        buggy.setCommitID(regression.getBuggy());
        regressionRevision.setBuggy(buggy);

        Revision work = new Revision();
        work.setRevisionName("work");
        work.setCommitID(regression.getWork());
        regressionRevision.setWork(work);

        //get projectFile
        File projectFile = sourceCodeManager.getMetaProjectDir(regression.getProjectUuid());
        checkout(regressionRevision, projectFile, userToken);
    }

    @Override
    public CodeDetails getFilesCode(String regressionUuid, String userToken, String filename, String oldPath, String newPath, String revisionFlag) {
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
                File bicFile = sourceCodeManager.getCacheProjectDir(userToken, regressionUuid, "bic", newPath);;
                newCode = sourceCodeManager.getRevisionCode(bicFile);
            }
        }
        codeDetails.setOldCode(oldCode);
        codeDetails.setNewCode(newCode);
        return codeDetails;
    }

    @Async
    public void checkout(RegressionRevision regressionRevision, File projectFile, String userToken) {
        String regressionUuid = regressionRevision.getRegressionUuid();

        //checkout: bfc version
        Revision bfc = regressionRevision.getBfc();
        File bfcDir = sourceCodeManager.checkout(bfc, projectFile, regressionUuid, userToken);
        bfc.setLocalCodeDir(bfcDir);

        //checkout: buggy version
        Revision buggy = regressionRevision.getBuggy();
        File buggyDir = sourceCodeManager.checkout(buggy, projectFile, regressionUuid, userToken);
        bfc.setLocalCodeDir(buggyDir);

        //checkout: bic version
        Revision bic = regressionRevision.getBic();
        File bicDir = sourceCodeManager.checkout(bic, projectFile, regressionUuid, userToken);
        bfc.setLocalCodeDir(bicDir);

        //checkout: work version
        Revision work = regressionRevision.getWork();
        File workDir = sourceCodeManager.checkout(work, projectFile, regressionUuid, userToken);
        bfc.setLocalCodeDir(workDir);
    }

    @Override
    public String runTest(String regressionUuid, String userToken,String revisionFlag){
        Regression regression = regressionMapper.getRegressionInfo(regressionUuid);
        // rfc revision
        Revision bfc = new Revision();
        bfc.setRevisionName("bfc");
        bfc.setLocalCodeDir(sourceCodeManager.getRevisionDir(regressionUuid, userToken, "bfc"));
        bfc.setCommitID(regression.getBfc());
        List<ChangedFile> bfcFiles = migrator.getChangedFiles(bfc.getLocalCodeDir(), regression.getBuggy(), regression.getBfc());
        bfc.setChangedFiles(bfcFiles);

        // need to migration revision
        File revisionFile = sourceCodeManager.getRevisionDir(regressionUuid, userToken, revisionFlag);
        Revision testMigrateRevision = new Revision();
        testMigrateRevision.setRevisionName(revisionFlag);
        testMigrateRevision.setLocalCodeDir(revisionFile);

        //test migration
        migrator.migrateTestAndDependency(bfc, testMigrateRevision, regression.getTestcase());

        Runner revisionRunner = new Runner(revisionFile, regression.getTestcase());
        return revisionRunner.getRunCode();
    }

    @Autowired
    public void setRegressionMapper(RegressionMapper regressionMapper) {
        this.regressionMapper = regressionMapper;
    }

}