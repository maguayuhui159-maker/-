package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.entity.AfterSaleRequest;
import com.jiangmai.platform.mapper.AfterSaleRequestMapper;
import com.jiangmai.platform.service.AfterSaleRequestService;
import org.springframework.stereotype.Service;

@Service
public class AfterSaleRequestServiceImpl extends ServiceImpl<AfterSaleRequestMapper, AfterSaleRequest> implements AfterSaleRequestService {
}
