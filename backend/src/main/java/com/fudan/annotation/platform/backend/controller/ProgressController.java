package com.fudan.annotation.platform.backend.controller;

import com.fudan.annotation.platform.backend.entity.ProgressInfo;
import com.fudan.annotation.platform.backend.service.ProgressService;
import com.fudan.annotation.platform.backend.service.RegressionService;
import com.fudan.annotation.platform.backend.vo.ResponseBean;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: sxz
 * @Date: 2022/05/31/23:05
 * @Description:
 */
@RestController
@ResponseBody
@RequestMapping(value = "/progress")
public class ProgressController {
    private  ProgressService progressService;
    private RegressionService regressionService;
    @GetMapping(value = "/info")
    public ResponseBean<ProgressInfo> getProgressInfo() {
        try {
            ProgressInfo progressInfo = progressService.getProgressInfo();
            progressInfo.setRegressionNum(regressionService.getRegressions(null,null).size());
            return  new ResponseBean<>(200, "get progress info success",null);
        } catch (Exception e) {
            return new ResponseBean<>(401, "get failed :" + e.getMessage(), null);
        }
    }
}
