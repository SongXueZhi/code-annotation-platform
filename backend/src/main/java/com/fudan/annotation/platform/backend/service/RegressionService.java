package com.fudan.annotation.platform.backend.service;

import com.fudan.annotation.platform.backend.entity.CodeDetails;
import com.fudan.annotation.platform.backend.entity.RegressionDetail;
import com.fudan.annotation.platform.backend.entity.Regression;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

public interface RegressionService {

    /**
     * description 获取所有regression
     *
     * @param regressionUuid   regressionID
     * @param regressionStatus regression状态
     */
    List<Regression> getRegressions(String regressionUuid, Integer regressionStatus, String projectName);

    /**
     * description 插入新regression
     *
     * @param regressionInfo regression信息
     */
    void insertRegression(Regression regressionInfo);

    /**
     * description 删除regression
     *
     * @param regressionUuid regressionUuid
     */
    void deleteRegression(String regressionUuid);

    /**
     * description 重置regression状态
     *
     * @param regressionUuid   regressionUuid
     * @param regressionStatus regression状态
     */
    void resetStatus(String regressionUuid, Integer regressionStatus);

    /**
     * description 添加项目uuid
     */
    void addProjectUuid();

    /**
     * description regression uuid
     */
    void addRegressionUuid();

    /**
     * description get changed files
     *
     * @param regressionUuid regressionUuid
     */
    RegressionDetail getChangedFiles(String regressionUuid, String userToken) throws Exception;

    RegressionDetail getMigrateInfo(String regressionUuid,String bic, String userToken) throws Exception;
    /**
     * description checkout
     *
     * @param regressionUuid regressionUuid
     * @param userToken      userToken
     */
    void checkoutByUser(String regressionUuid, String userToken);

    /**
     * description get files code
     *
     * @param regressionUuid regressionUuid
     * @param userToken      userToken
     * @param filename       filename
     * @param oldPath        oldPath
     * @param newPath        newPath
     * @param revisionFlag   revisionFlag
     */
    CodeDetails getFilesCode(String regressionUuid, String userToken, String filename, String oldPath, String newPath, String revisionFlag);

    String runTest(String regressionUuid, String userToken, String revisionFlag);

    String readRuntimeResult(String filaPath) throws IOException;
}
