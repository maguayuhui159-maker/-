package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.entity.WorkViewHistory;
import com.jiangmai.platform.mapper.WorkViewHistoryMapper;
import com.jiangmai.platform.service.WorkViewHistoryService;
import org.springframework.stereotype.Service;

@Service
public class WorkViewHistoryServiceImpl extends ServiceImpl<WorkViewHistoryMapper, WorkViewHistory> implements WorkViewHistoryService {
}
