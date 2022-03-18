package com.fudan.annotation.platform.backend.controller;

import com.fudan.annotation.platform.backend.entity.CodeDetails;
import com.fudan.annotation.platform.backend.entity.RegressionDetail;
import com.fudan.annotation.platform.backend.entity.Regression;
import com.fudan.annotation.platform.backend.service.RegressionService;
import com.fudan.annotation.platform.backend.vo.ResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * description: regression controller
 *
 * @author Richy
 * create: 2021-12-10 16:02
 **/

@RestController
@ResponseBody
@RequestMapping(value = "/regression")
public class RegressionController {
    private RegressionService regressionService;

    @GetMapping(value = "/all")
    public ResponseBean<List<Regression>> getAllRegressions(
            @RequestParam(name = "regression_uuid", required = false) String regressionUuid,
            @RequestParam(name = "regression_status", required = false) Integer regressionStatus) {
        try {
            List<Regression> regressionList = regressionService.getRegressions(regressionUuid, regressionStatus);
            return new ResponseBean<>(200, "get regression info success", regressionList);
        } catch (Exception e) {
            return new ResponseBean<>(401, "get failed :" + e.getMessage(), null);
        }
    }

    @PostMapping(value = "/add")
    public ResponseBean addRegression(@RequestBody Regression regression) {
        try {
            regressionService.insertRegression(regression);
            return new ResponseBean<>(200, "add regression success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "add regression failed :" + e.getMessage(), null);
        }
    }

    @DeleteMapping(value = "/delete")
    public ResponseBean deleteRegression(@RequestParam("regression_uuid") String regressionUuid) {
        try {
            regressionService.deleteRegression(regressionUuid);
            return new ResponseBean<>(200, "delete success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "delete failed :" + e.getMessage(), null);
        }
    }

    @PutMapping(value = "/status")
    public ResponseBean resetStatus(@RequestParam(name = "regression_uuid") String regressionUuid,
                                    @RequestParam(name = "regression_status") Integer regressionStatus) {
        try {
            regressionService.resetStatus(regressionUuid, regressionStatus);
            return new ResponseBean<>(200, "reset success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "reset failed :" + e.getMessage(), null);
        }
    }

    @PutMapping(value = "/project_uuid")
    public ResponseBean addProjectUuid() {
        try {
            regressionService.addProjectUuid();
            return new ResponseBean<>(200, "add project uuid success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "add project uuid failed :" + e.getMessage(), null);
        }
    }

    @PutMapping(value = "/regression_uuid")
    public ResponseBean addRegressionUuid() {
        try {
            regressionService.addRegressionUuid();
            return new ResponseBean<>(200, "add regression uuid success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "add regression uuid failed :" + e.getMessage(), null);
        }
    }

    @GetMapping(value = "/detail")
    public ResponseBean<RegressionDetail> getChangedFiles(
            @RequestParam(name = "regression_uuid") String regressionUuid,
            @RequestParam String userToken) {
        try {
            RegressionDetail changedFiles = regressionService.getChangedFiles(regressionUuid,userToken);
            return new ResponseBean<>(200, "get regression info success", changedFiles);
        } catch (Exception e) {
            return new ResponseBean<>(401, "get failed :" + e.getMessage(), null);
        }
    }

    @PutMapping(value = "/checkout")
    public ResponseBean<RegressionDetail> checkoutByUser(
            @RequestParam(name = "regression_uuid") String regressionUuid,
            @RequestParam String userToken) {
        try {
            regressionService.checkoutByUser(regressionUuid, userToken);
            return new ResponseBean<>(200, "checkout success", null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "checkout failed :" + e.getMessage(), null);
        }
    }

    @GetMapping(value = "/code")
    public ResponseBean<CodeDetails> getCode(
            @RequestParam(name = "regression_uuid") String regressionUuid,
            @RequestParam String userToken,
            @RequestParam String filename,
            @RequestParam(name = "old_path") String oldPath,
            @RequestParam(name = "new_path") String newPath,
            @RequestParam String revisionFlag) {
        try {
            CodeDetails revisionCode = regressionService.getFilesCode(regressionUuid, userToken, filename, oldPath, newPath, revisionFlag);
            return new ResponseBean<>(200, "get code success", revisionCode);
        } catch (Exception e) {
            return new ResponseBean<>(401, "get code failed :" + e.getMessage(), null);
        }
    }

    @GetMapping(value = "/console")
    public ResponseBean<String> getConsoleResult(
            @RequestParam(name = "path") String path) {
        try {
            String revisionRunResult = regressionService.readRuntimeResult(URLDecoder.decode(path,"UTF-8"));
            return new ResponseBean<>(200, "get result success", revisionRunResult);
        } catch (Exception e) {
            return new ResponseBean<>(401, "get result failed :" + e.getMessage(), null);
        }
    }
    @GetMapping(value = "/test")
    public ResponseBean<String> test(
            @RequestParam(name = "regression_uuid") String regressionUuid,
            @RequestParam String userToken,
            @RequestParam String revisionFlag) {
        try {
            String logPath = regressionService.runTest(regressionUuid, userToken, revisionFlag);
            logPath = URLEncoder.encode(logPath,"UTF-8");
            return new ResponseBean<>(200, "test success", logPath);
        } catch (Exception e) {
            return new ResponseBean<>(401, "test failed :" + e.getMessage(), null);
        }
    }

    @Autowired
    public void setRegressionService(RegressionService regressionService) {
        this.regressionService = regressionService;
    }
}