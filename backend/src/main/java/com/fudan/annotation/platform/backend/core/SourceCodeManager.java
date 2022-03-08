package com.fudan.annotation.platform.backend.core;

import com.fudan.annotation.platform.backend.entity.Revision;
import com.fudan.annotation.platform.backend.util.GitUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * description:
 *
 * @author Richy
 * create: 2022-02-23 20:00
 **/
@Component
public class SourceCodeManager {

    @Value("${local.workSpace}")
    private String workSpace;

    private String metaProjectsDirPath = "E:\\reg\\regminerTool\\test-transfer-space" + File.separator + "meta_projects";
    private String cacheProjectsDirPath = "E:\\reg\\regminerTool\\test-transfer-space" + File.separator + "transfer_cache";

    public File getMetaProjectDir(String projectUuid) {
        return new File(metaProjectsDirPath + File.separator + projectUuid);
    }

    public File checkout(Revision revision, File projectFile, String regressionUuid, String userToken) {
        //copy source code from meta project dir
        File projectCacheDir = new File(cacheProjectsDirPath + File.separator + userToken + File.separator + regressionUuid);
        if (projectCacheDir.exists() && !projectCacheDir.isDirectory()) {
            projectCacheDir.delete();
        }
        projectCacheDir.mkdirs();
        String projectUuid = "491245a5-9e6f-4e4a-bf94-59cfb6f352a1" ;

        File revisionDir = new File(projectCacheDir, revision.getRevisionName());
        try {
            if (revisionDir.exists()) {
                FileUtils.forceDelete(revisionDir);
            }
            FileUtils.copyDirectoryToDirectory(projectFile, projectCacheDir);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
        new File(projectCacheDir, projectUuid).renameTo(revisionDir);
        //git checkout
        if (GitUtil.checkout(revision.getCommitID(), revisionDir)) {
            return revisionDir;
        }
        return null;
    }

    public File getCacheProjectDir(String userToken, String regressionUuid, String revisionFlag, String filePath) {
            return new File(cacheProjectsDirPath + File.separator + userToken + File.separator +
                regressionUuid + File.separator + revisionFlag + File.separator + filePath);
    }

    public File getRevisionDir(String regressionUuid, String userToken,String revisionFlag){
        return new File(cacheProjectsDirPath + File.separator + userToken + File.separator +
                regressionUuid + File.separator + revisionFlag);
    }

    public String getRevisionCode(File revisionFile) {
//        方法1：
        String str = "";
        try {
            str = FileUtils.readFileToString(revisionFile);
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(str);

        return str;

//         //方法2
//        StringBuffer stringBuffer = new StringBuffer();
//        BufferedReader bfReader = null;
//        String revisionCode = "";
//        int line = 1;
//        try {
////            BufferedReader bf = new BufferedReader(new FileReader(revisionFile));
//            if (revisionFile.exists() && revisionFile.isFile()) {
//                FileInputStream fileIn = new FileInputStream(revisionFile);
//                bfReader = new BufferedReader(new InputStreamReader(fileIn));
//                while ((revisionCode = bfReader.readLine()) != null) {
//                    stringBuffer.append("\r\n"+revisionCode);
//                    line++;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (bfReader != null) {
//                try {
//                    bfReader.close();
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//        System.out.println(stringBuffer.toString());
//        return stringBuffer.toString();

    }

}