package com.jiangmai.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiangmai.platform.dto.WorkDTO;
import com.jiangmai.platform.entity.Work;

public interface WorkService extends IService<Work> {
    void submitWork(WorkDTO workDTO, Long authorId);
}
