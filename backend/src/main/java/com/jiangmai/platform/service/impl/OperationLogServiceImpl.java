package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.entity.OperationLog;
import com.jiangmai.platform.mapper.OperationLogMapper;
import com.jiangmai.platform.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
