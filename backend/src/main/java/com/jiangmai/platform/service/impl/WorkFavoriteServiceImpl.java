package com.jiangmai.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiangmai.platform.entity.WorkFavorite;
import com.jiangmai.platform.mapper.WorkFavoriteMapper;
import com.jiangmai.platform.service.WorkFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class WorkFavoriteServiceImpl extends ServiceImpl<WorkFavoriteMapper, WorkFavorite> implements WorkFavoriteService {
}
