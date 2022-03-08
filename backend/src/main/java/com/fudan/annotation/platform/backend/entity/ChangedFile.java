package com.fudan.annotation.platform.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author Richy
 * create: 2022-03-03 17:06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangedFile {
    private String filename;
    private String newPath;
    private String oldPath;
    private Type type;

    public ChangedFile(String newPath) {
        this.newPath = newPath;
    }

    public enum Type {
        TEST_SUITE, TEST_RELATE, JAVA_FILE, ANOTHER
    }
}