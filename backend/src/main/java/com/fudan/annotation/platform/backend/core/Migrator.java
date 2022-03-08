package com.fudan.annotation.platform.backend.core;

import com.fudan.annotation.platform.backend.entity.ChangedFile;
import com.fudan.annotation.platform.backend.entity.Revision;
import com.fudan.annotation.platform.backend.entity.file.NormalFile;
import com.fudan.annotation.platform.backend.entity.file.SourceFile;
import com.fudan.annotation.platform.backend.entity.file.TestFile;
import com.fudan.annotation.platform.backend.entity.file.TestRelatedFile;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.diff.DiffEntry;
import com.fudan.annotation.platform.backend.util.GitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * description:
 *
 * @author Richy
 * create: 2022-02-23 20:21
 **/

@Component
public class Migrator {
    public final static String NONE_PATH = "/dev/null";
    @Autowired
    private Reducer reducer;

    public List<ChangedFile> getChangedFiles(File projectFile, String newID, String oldID) {
        List<DiffEntry> diffEntries = GitUtil.getDiffEntriesBetweenCommits(projectFile, newID, oldID);
        List<ChangedFile> changedFiles = new ArrayList<>();
        for (DiffEntry diffEntry : diffEntries) {
            String newPath = diffEntry.getNewPath();
            String oldPath = diffEntry.getOldPath();
            String newFile = newPath.substring(newPath.lastIndexOf("/") + 1);
            if (!newFile.equals("null") && !newFile.equals("CHANGES")) {
                ChangedFile changedFile = new ChangedFile();
                changedFile.setFilename(newFile);
                changedFile.setOldPath(oldPath);
                changedFile.setNewPath(newPath);
                changedFiles.add(changedFile);
            }
        }
        return changedFiles;
    }

    public void migrateTestAndDependency(Revision bfc, Revision testMigrateRevision, String testCase) {
        List<DiffEntry> diffEntries = GitUtil.getDiffEntriesBetweenCommits(bfc.getLocalCodeDir(), bfc.getCommitID(), bfc.getCommitID() + "~1");

        diffEntries.forEach(diffEntry -> {
            addChangedFileToBfc(diffEntry, bfc);
        });
        reducer.reduceTestCases(bfc, testCase);
        migrateTestFromTo_0(bfc, testMigrateRevision);
    }

    public void migrateTestFromTo_0(Revision from, Revision to) {
        File bfcDir = from.getLocalCodeDir();
        File tDir = to.getLocalCodeDir();

        for (ChangedFile changedFile : from.getChangedFiles()) {

            if (changedFile instanceof NormalFile) {
                return;
            }

            String newPath = changedFile.getNewPath();
            if (newPath.contains(NONE_PATH)) {
                continue;
            }
            File bfcFile = new File(bfcDir, newPath);
            File tFile = new File(tDir, newPath);
            if (tFile.exists()) {
                tFile.deleteOnExit();
            }
            // 直接copy过去
            try {
                FileUtils.forceMkdirParent(tFile);
                FileUtils.copyFileToDirectory(bfcFile, tFile.getParentFile());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void addChangedFileToBfc(DiffEntry entry, Revision rfc) {
        ChangedFile file = null;
        String path = entry.getNewPath();
        if (path.contains("test") && path.endsWith(".java")) {
            String testCode = null;
            try {
                testCode = FileUtils.readFileToString(new File(rfc.getLocalCodeDir(), path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (testCode.contains("@Test") || testCode.contains("junit")) {
                file = new TestFile(path);
            } else {
                file = new TestRelatedFile(path);
            }
        }

        if (!path.endsWith(".java") && !path.endsWith("pom.xml") && !path.contains("gradle")) {
            file = new SourceFile(path);
        }
        if (file != null) {
            file.setOldPath(entry.getOldPath());
            rfc.getChangedFiles().add(file);
        }
    }

}