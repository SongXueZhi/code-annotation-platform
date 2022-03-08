package com.fudan.annotation.platform.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author Richy
 * create: 2022-02-25 14:54
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegressionRevision {
    private String regressionUuid;
    private Revision bfc;
    private Revision buggy;
    private Revision bic;
    private Revision work;
    private String testCase;
}