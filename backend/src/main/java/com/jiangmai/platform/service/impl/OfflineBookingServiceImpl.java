package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.entity.OfflineBooking;
import com.jiangmai.platform.mapper.OfflineBookingMapper;
import com.jiangmai.platform.service.OfflineBookingService;
import org.springframework.stereotype.Service;

@Service
public class OfflineBookingServiceImpl extends ServiceImpl<OfflineBookingMapper, OfflineBooking> implements OfflineBookingService {
}
