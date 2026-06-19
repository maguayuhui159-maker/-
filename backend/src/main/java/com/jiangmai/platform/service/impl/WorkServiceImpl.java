package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.dto.WorkDTO;
import com.jiangmai.platform.entity.Work;
import com.jiangmai.platform.mapper.WorkMapper;
import com.jiangmai.platform.service.WorkService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {

    @Override
    public void submitWork(WorkDTO workDTO, Long authorId) {
        Work work = new Work();
        work.setTitle(workDTO.getTitle());
        work.setDescription(workDTO.getDescription());
        work.setImageUrl(workDTO.getImageUrl());
        work.setIsForSale(workDTO.getIsForSale() != null ? workDTO.getIsForSale() : 0);
        work.setPrice(workDTO.getPrice());
        work.setStatus("PENDING"); // always pending initially
        work.setAuthorId(authorId);
        work.setCreateTime(LocalDateTime.now());
        
        this.save(work);
    }
}
