package com.fudan.annotation.platform.backend.service.impl;

import com.fudan.annotation.platform.backend.entity.ProgressInfo;
import com.fudan.annotation.platform.backend.entity.SearchDetails;
import com.fudan.annotation.platform.backend.service.ProgressService;
import com.fudan.annotation.platform.backend.util.ProgressUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author: sxz
 * @Date: 2022/06/03/14:50
 * @Description:
 */
@Service
@Slf4j
public class ProgressServiceImpl implements ProgressService {

    @Override
    public ProgressInfo getProgressInfo() throws IOException {
        return ProgressUtils.getRegMinerProgress();

    }

    @Override
    public SearchDetails getSearchDetails(String projectName, String bfc) throws IOException {
        return ProgressUtils.getSearchDetails(projectName,bfc);
    }


}
