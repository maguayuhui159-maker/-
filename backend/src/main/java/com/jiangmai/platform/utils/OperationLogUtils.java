package com.jiangmai.platform.utils;

import com.jiangmai.platform.entity.OperationLog;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.service.OperationLogService;

import java.time.LocalDateTime;

public class OperationLogUtils {

    private OperationLogUtils() {
    }

    public static void record(OperationLogService operationLogService,
                              User actor,
                              String module,
                              String action,
                              Long targetId,
                              String detail) {
        if (operationLogService == null || actor == null) {
            return;
        }
        try {
            OperationLog log = new OperationLog();
            log.setActorId(actor.getId());
            log.setActorName(actor.getUsername());
            log.setActorRole(actor.getRole());
            log.setModule(module);
            log.setAction(action);
            log.setTargetId(targetId);
            log.setDetail(detail);
            log.setCreateTime(LocalDateTime.now());
            operationLogService.save(log);
        } catch (Exception ignored) {
        }
    }
}
