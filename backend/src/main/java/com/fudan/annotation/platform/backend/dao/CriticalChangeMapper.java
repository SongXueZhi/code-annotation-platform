package com.fudan.annotation.platform.backend.dao;

import com.fudan.annotation.platform.backend.entity.HunkEntity;
import com.fudan.annotation.platform.backend.entity.HunkInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface CriticalChangeMapper {

    void setHunks(HunkInfo hunkInfo);
}
