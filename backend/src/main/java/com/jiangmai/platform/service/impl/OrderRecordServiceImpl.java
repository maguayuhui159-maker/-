package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.entity.OrderRecord;
import com.jiangmai.platform.mapper.OrderRecordMapper;
import com.jiangmai.platform.service.OrderRecordService;
import org.springframework.stereotype.Service;

@Service
public class OrderRecordServiceImpl extends ServiceImpl<OrderRecordMapper, OrderRecord> implements OrderRecordService {
}
