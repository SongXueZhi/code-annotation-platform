package com.fudan.annotation.platform.backend.service.impl;

import com.fudan.annotation.platform.backend.entity.ProgressInfo;
import com.fudan.annotation.platform.backend.service.ProgressService;
import com.fudan.annotation.platform.backend.util.ProgressUtils;

import java.io.IOException;

/**
 * @Author: sxz
 * @Date: 2022/06/03/14:50
 * @Description:
 */
public class ProgressServiceImpl implements ProgressService {

    @Override
    public ProgressInfo getProgressInfo() throws IOException {
        return ProgressUtils.getRegMinerProgress();

    }
}
