package com.fudan.annotation.platform.backend.dao;

import com.fudan.annotation.platform.backend.entity.Hunk;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface CriticalChangeMapper {

    void setHunks(String regressionUuid, Hunk hunkDTO);
}
