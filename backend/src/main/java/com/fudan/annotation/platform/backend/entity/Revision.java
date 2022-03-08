package com.fudan.annotation.platform.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * description:
 *
 * @author Richy
 * create: 2022-02-23 20:23
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Revision {
    /**
     * 1个regression包含四个Revision（bic\bfc\buggy\work）
     * */

    File localCodeDir = null;
    private String revisionName;
    private String commitID;
    private List<ChangedFile> changedFiles = new LinkedList<>();
}