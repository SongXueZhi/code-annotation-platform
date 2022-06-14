package com.fudan.annotation.platform.backend.util;

import com.fudan.annotation.platform.backend.entity.ProgressInfo;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * @Author: sxz
 * @Date: 2022/06/03/15:13
 * @Description:
 */
public class ProgressUtils {
    private final static String WORK_SPACE = System.getProperty("user.dir") + File.separator + "miner_space";
    private final static String PROJECT_LIST_FILE = "project.in";
    private final static String PROJECT_POSTFIX = ".project";
    private final static String NONE = "N.A.";
    private final static String PRFC_TOTAL_PREFIX = "pRFC total:";
    private final static String PROGRESS_FILE = "progress.details";

    public static ProgressInfo getRegMinerProgress() throws IOException {
        ProgressInfo progressInfo = new ProgressInfo();
        File workDir = new File(WORK_SPACE);
        // project info
        progressInfo.setTotalProjectNum(FileUtils.readLines(new File(workDir, PROJECT_LIST_FILE),
                StandardCharsets.UTF_8).size());
        String[] currentProjects = workDir.list((dir, name) -> name.endsWith(PROJECT_POSTFIX));
        progressInfo.setCurrentProjectName(
                (currentProjects == null || currentProjects.length == 0)
                        ? currentProjects[0] : NONE);

        File projectFile = new File(workDir,currentProjects[0].replace(PROJECT_POSTFIX,""));
        List<String> projectInfo = progressInfo.getCurrentProjectName().equals(NONE)
                ? null : FileUtils.readLines(projectFile,StandardCharsets.UTF_8);

        progressInfo.setProjectQueueNum(
                (!progressInfo.getCurrentProjectName().equals(NONE)) && progressInfo!=null && projectInfo.size()>1
                        ?  projectInfo.get(1) : NONE
        );

        progressInfo.setProjectStatTime(
                (!progressInfo.getCurrentProjectName().equals(NONE)) && progressInfo!=null && projectInfo.size()>0
                        ? projectInfo.get(0):NONE
        );

        //rPFC
        List<String> pRFCList = FileUtils.readLines(new File(
                workDir,progressInfo.getCurrentProjectName()),StandardCharsets.UTF_8);

        Iterator<String> iterator = pRFCList.iterator();
        while (iterator.hasNext()){
            if (!iterator.next().startsWith(PRFC_TOTAL_PREFIX)){
                iterator.remove();
            }
        }
        progressInfo.setTotalPRFCNum(pRFCList.get(pRFCList.size()).split(PRFC_TOTAL_PREFIX)[1]);

        progressInfo.setPRFCDoneNum((!progressInfo.getCurrentProjectName().equals(NONE)) ? FileUtils.readLines(
                new File(projectFile,PROGRESS_FILE), StandardCharsets.UTF_8).size() : 0
                );
        return progressInfo;
    }
}
