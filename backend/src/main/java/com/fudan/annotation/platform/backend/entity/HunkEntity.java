package com.fudan.annotation.platform.backend.entity;

import java.io.Serializable;

/**
 * @Author: sxz
 * @Date: 2022/06/27/10:50
 * @Description:
 */
public class HunkEntity implements Serializable {
    private  String  newPath;
    private  String  oldPath;
    private  int  beginA;
    private  int  beginB;
    private  int  endA;
    private  int  endB;
    private String type;
}
