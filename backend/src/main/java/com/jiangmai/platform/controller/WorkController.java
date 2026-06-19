package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangmai.platform.dto.WorkDTO;
import com.jiangmai.platform.entity.OperationLog;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.entity.Work;
import com.jiangmai.platform.entity.WorkFavorite;
import com.jiangmai.platform.entity.WorkViewHistory;
import com.jiangmai.platform.service.OperationLogService;
import com.jiangmai.platform.service.UserService;
import com.jiangmai.platform.service.WorkFavoriteService;
import com.jiangmai.platform.service.WorkService;
import com.jiangmai.platform.service.WorkViewHistoryService;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.OperationLogUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    @Autowired
    private WorkService workService;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkFavoriteService workFavoriteService;

    @Autowired
    private WorkViewHistoryService workViewHistoryService;

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    public Result<List<Work>> listWorks() {
        QueryWrapper<Work> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "APPROVED").orderByDesc("create_time");
        return Result.success(workService.list(wrapper));
    }

    @GetMapping("/all")
    public Result<List<Work>> listAllWorks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        QueryWrapper<Work> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (currentUser == null) {
            wrapper.eq("status", "APPROVED");
        } else if (!"ADMIN".equals(currentUser.getRole())) {
            wrapper.eq("author_id", currentUser.getId());
        }
        return Result.success(workService.list(wrapper));
    }

    @GetMapping("/my")
    public Result<List<Work>> listMyWorks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        QueryWrapper<Work> wrapper = new QueryWrapper<>();
        wrapper.eq("author_id", currentUser.getId()).orderByDesc("create_time");
        return Result.success(workService.list(wrapper));
    }

    @PostMapping("/upload")
    public Result<String> uploadWork(@RequestBody WorkDTO workDTO,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        if ("ADMIN".equals(currentUser.getRole())) {
            return Result.error("管理员不能以学员身份上传作品");
        }

        try {
            workService.submitWork(workDTO, currentUser.getId());
            OperationLogUtils.record(operationLogService, currentUser, "WORK", "UPLOAD", null, "提交作品《" + workDTO.getTitle() + "》");
            return Result.success("上传作品成功等待平台审核");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    public Result<String> reviewWork(@PathVariable Long id,
                                     @RequestBody Map<String, String> body,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        if (!"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可审核作品");
        }

        String newStatus = String.valueOf(body.getOrDefault("status", "")).trim().toUpperCase();
        if (!"APPROVED".equals(newStatus) && !"REJECTED".equals(newStatus)) {
            return Result.error("审核状态非法");
        }

        Work work = workService.getById(id);
        if (work == null) {
            return Result.error("作品不存在");
        }
        work.setStatus(newStatus);
        workService.updateById(work);
        OperationLogUtils.record(operationLogService, currentUser, "WORK", "REVIEW", id, "作品《" + work.getTitle() + "》审核为 " + newStatus);
        return Result.success("审核操作成功");
    }

    @PostMapping("/{id}/favorite")
    public Result<String> favoriteWork(@PathVariable Long id,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        Work work = workService.getById(id);
        if (work == null || !"APPROVED".equals(work.getStatus())) {
            return Result.error("作品不存在或暂不可收藏");
        }

        QueryWrapper<WorkFavorite> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).eq("work_id", id).last("limit 1");
        WorkFavorite existing = workFavoriteService.getOne(query);
        if (existing != null) {
            return Result.success("已收藏该作品");
        }

        WorkFavorite favorite = new WorkFavorite();
        favorite.setUserId(currentUser.getId());
        favorite.setWorkId(id);
        favorite.setCreateTime(LocalDateTime.now());
        workFavoriteService.save(favorite);
        OperationLogUtils.record(operationLogService, currentUser, "WORK", "FAVORITE", id, "收藏作品《" + work.getTitle() + "》");
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{id}/favorite")
    public Result<String> unfavoriteWork(@PathVariable Long id,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }

        QueryWrapper<WorkFavorite> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).eq("work_id", id);
        workFavoriteService.remove(query);
        OperationLogUtils.record(operationLogService, currentUser, "WORK", "UNFAVORITE", id, "取消收藏作品 ID=" + id);
        return Result.success("已取消收藏");
    }

    @GetMapping("/favorites/my")
    public Result<List<Map<String, Object>>> myFavoriteWorks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        QueryWrapper<WorkFavorite> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).orderByDesc("create_time");
        List<WorkFavorite> favorites = workFavoriteService.list(query);
        return Result.success(enrichFavoriteWorks(favorites));
    }

    @PostMapping("/{id}/view")
    public Result<String> recordView(@PathVariable Long id,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.success("游客浏览不记录历史");
        }
        Work work = workService.getById(id);
        if (work == null || (!"APPROVED".equals(work.getStatus()) && !currentUser.getId().equals(work.getAuthorId()) && !"ADMIN".equals(currentUser.getRole()))) {
            return Result.error("作品不存在");
        }

        QueryWrapper<WorkViewHistory> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).eq("work_id", id).last("limit 1");
        WorkViewHistory history = workViewHistoryService.getOne(query);
        if (history == null) {
            history = new WorkViewHistory();
            history.setUserId(currentUser.getId());
            history.setWorkId(id);
            history.setViewCount(1);
            history.setCreateTime(LocalDateTime.now());
        } else {
            int currentCount = history.getViewCount() == null ? 0 : history.getViewCount();
            history.setViewCount(currentCount + 1);
        }
        history.setLastViewTime(LocalDateTime.now());
        history.setUpdateTime(LocalDateTime.now());
        workViewHistoryService.saveOrUpdate(history);
        return Result.success("浏览记录已更新");
    }

    @GetMapping("/recent-viewed/my")
    public Result<List<Map<String, Object>>> myRecentViewedWorks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        QueryWrapper<WorkViewHistory> query = new QueryWrapper<>();
        query.eq("user_id", currentUser.getId()).orderByDesc("last_view_time");
        query.last("limit 6");
        List<WorkViewHistory> histories = workViewHistoryService.list(query);
        return Result.success(enrichViewedWorks(histories));
    }

    private User currentUser(String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        return userId == null ? null : userService.getById(userId);
    }

    private List<Map<String, Object>> enrichFavoriteWorks(List<WorkFavorite> favorites) {
        if (favorites == null || favorites.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> workIds = favorites.stream().map(WorkFavorite::getWorkId).collect(Collectors.toList());
        Map<Long, Work> workMap = workService.listByIds(workIds).stream()
                .filter(work -> "APPROVED".equals(work.getStatus()))
                .collect(Collectors.toMap(Work::getId, work -> work, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (WorkFavorite favorite : favorites) {
            Work work = workMap.get(favorite.getWorkId());
            if (work == null) {
                continue;
            }
            payload.add(toWorkPayload(work, favorite.getCreateTime(), null, null));
        }
        return payload;
    }

    private List<Map<String, Object>> enrichViewedWorks(List<WorkViewHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> workIds = histories.stream().map(WorkViewHistory::getWorkId).collect(Collectors.toSet());
        Map<Long, Work> workMap = workService.listByIds(workIds).stream()
                .collect(Collectors.toMap(Work::getId, work -> work, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (WorkViewHistory history : histories) {
            Work work = workMap.get(history.getWorkId());
            if (work == null) {
                continue;
            }
            payload.add(toWorkPayload(work, null, history.getLastViewTime(), history.getViewCount()));
        }
        return payload;
    }

    private Map<String, Object> toWorkPayload(Work work, LocalDateTime favoriteTime, LocalDateTime lastViewTime, Integer viewCount) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", work.getId());
        row.put("title", work.getTitle());
        row.put("description", work.getDescription());
        row.put("imageUrl", work.getImageUrl());
        row.put("authorId", work.getAuthorId());
        row.put("status", work.getStatus());
        row.put("isForSale", work.getIsForSale());
        row.put("price", work.getPrice());
        row.put("createTime", work.getCreateTime());
        row.put("updateTime", work.getUpdateTime());
        row.put("favoriteTime", favoriteTime);
        row.put("lastViewTime", lastViewTime);
        row.put("viewCount", viewCount == null ? 0 : viewCount);
        return row;
    }
}
