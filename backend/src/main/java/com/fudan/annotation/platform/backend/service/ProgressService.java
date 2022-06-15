package com.fudan.annotation.platform.backend.service;

import com.fudan.annotation.platform.backend.entity.ProgressInfo;
import com.fudan.annotation.platform.backend.entity.SearchDetails;

import java.io.IOException;

/**
 * @Author: sxz
 * @Date: 2022/06/03/14:48
 * @Description:
 */
public interface ProgressService {
    ProgressInfo getProgressInfo() throws IOException;
    SearchDetails getSearchDetails(String projectName, String bfc) throws IOException;
}
