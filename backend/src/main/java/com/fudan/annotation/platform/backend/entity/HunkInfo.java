package com.fudan.annotation.platform.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author Richy
 * create: 2022-06-27 21:58
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HunkInfo {
    String revisionName;
    private String regressionUuid;
    private  String  newPath;
    private  String  oldPath;
    private  int  beginA;
    private  int  beginB;
    private  int  endA;
    private  int  endB;
    private String type;
}