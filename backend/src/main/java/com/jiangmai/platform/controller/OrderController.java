package com.jiangmai.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiangmai.platform.entity.AfterSaleRequest;
import com.jiangmai.platform.entity.OrderRecord;
import com.jiangmai.platform.entity.User;
import com.jiangmai.platform.entity.Work;
import com.jiangmai.platform.service.AfterSaleRequestService;
import com.jiangmai.platform.service.OperationLogService;
import com.jiangmai.platform.service.OrderRecordService;
import com.jiangmai.platform.service.UserService;
import com.jiangmai.platform.service.WorkService;
import com.jiangmai.platform.utils.AuthUtils;
import com.jiangmai.platform.utils.OperationLogUtils;
import com.jiangmai.platform.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRecordService orderRecordService;

    @Autowired
    private AfterSaleRequestService afterSaleRequestService;

    @Autowired
    private WorkService workService;

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    @PostMapping
    public Result<String> createOrder(@RequestBody Map<String, Long> body,
                                      @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }
        Long workId = body.get("workId");
        if (workId == null) {
            return Result.error("缺少作品ID");
        }

        Work work = workService.getById(workId);
        if (work == null || !"APPROVED".equals(work.getStatus()) || !Objects.equals(work.getIsForSale(), 1)) {
            return Result.error("作品不可购买");
        }
        if (Objects.equals(work.getAuthorId(), currentUser.getId())) {
            return Result.error("不能购买自己上传的作品");
        }

        QueryWrapper<OrderRecord> duplicateQuery = new QueryWrapper<>();
        duplicateQuery.eq("buyer_id", currentUser.getId()).eq("work_id", workId).ne("status", "REFUNDED").last("limit 1");
        if (orderRecordService.getOne(duplicateQuery) != null) {
            return Result.success("该作品已有有效订单");
        }

        try {
            OrderRecord order = new OrderRecord();
            order.setOrderNo("OD" + System.currentTimeMillis());
            order.setBuyerId(currentUser.getId());
            order.setWorkId(workId);
            order.setAmount(work.getPrice());
            order.setStatus("PAID");
            order.setRemark("线上商城下单");
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderRecordService.save(order);
            OperationLogUtils.record(operationLogService, currentUser, "ORDER", "CREATE", order.getId(), "下单作品《" + work.getTitle() + "》");
            return Result.success("下单成功");
        } catch (Exception e) {
            return Result.error("下单失败，请稍后重试");
        }
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }

        QueryWrapper<OrderRecord> query = new QueryWrapper<>();
        query.eq("buyer_id", currentUser.getId()).orderByDesc("create_time");
        return Result.success(enrichOrders(orderRecordService.list(query)));
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> allOrders(@RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可查看全部订单");
        }

        QueryWrapper<OrderRecord> query = new QueryWrapper<>();
        if (status != null && !status.isBlank()) {
            query.eq("status", status.trim().toUpperCase());
        }
        if (keyword != null && !keyword.isBlank()) {
            query.and(w -> w.like("order_no", keyword).or().like("remark", keyword));
        }
        query.orderByDesc("create_time");
        return Result.success(enrichOrders(orderRecordService.list(query)));
    }

    @PutMapping("/{id}/status")
    public Result<String> updateOrderStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可更新订单");
        }

        OrderRecord order = orderRecordService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        String status = String.valueOf(body.getOrDefault("status", order.getStatus())).toUpperCase();
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderRecordService.updateById(order);
        OperationLogUtils.record(operationLogService, currentUser, "ORDER", "UPDATE_STATUS", id, "订单状态更新为 " + status);
        return Result.success("订单状态已更新");
    }

    @PostMapping("/{id}/aftersale")
    public Result<String> applyAfterSale(@PathVariable Long id,
                                         @RequestBody Map<String, String> body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null) {
            return Result.error("未登录或登录已失效");
        }

        OrderRecord order = orderRecordService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!Objects.equals(order.getBuyerId(), currentUser.getId())) {
            return Result.error("仅买家可申请售后");
        }

        QueryWrapper<AfterSaleRequest> duplicate = new QueryWrapper<>();
        duplicate.eq("order_id", id).eq("status", "PENDING");
        if (afterSaleRequestService.count(duplicate) > 0) {
            return Result.error("该订单已有待处理售后申请");
        }

        try {
            AfterSaleRequest request = new AfterSaleRequest();
            request.setOrderId(id);
            request.setUserId(currentUser.getId());
            request.setType(String.valueOf(body.getOrDefault("type", "REFUND")));
            request.setReason(String.valueOf(body.getOrDefault("reason", "用户发起售后")));
            request.setStatus("PENDING");
            request.setCreateTime(LocalDateTime.now());
            request.setUpdateTime(LocalDateTime.now());
            afterSaleRequestService.save(request);

            order.setStatus("REFUNDING");
            order.setUpdateTime(LocalDateTime.now());
            orderRecordService.updateById(order);
            OperationLogUtils.record(operationLogService, currentUser, "ORDER", "AFTERSALE_APPLY", id, "提交售后申请");
            return Result.success("售后申请已提交");
        } catch (Exception e) {
            return Result.error("售后申请提交失败");
        }
    }

    @GetMapping("/aftersale/all")
    public Result<List<Map<String, Object>>> afterSaleList(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可查看售后");
        }

        QueryWrapper<AfterSaleRequest> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        List<AfterSaleRequest> requests = afterSaleRequestService.list(query);

        Set<Long> orderIds = requests.stream().map(AfterSaleRequest::getOrderId).collect(Collectors.toSet());
        Map<Long, OrderRecord> orderMap = orderIds.isEmpty()
                ? Collections.emptyMap()
                : orderRecordService.listByIds(orderIds).stream().collect(Collectors.toMap(OrderRecord::getId, o -> o, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (AfterSaleRequest request : requests) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", request.getId());
            row.put("orderId", request.getOrderId());
            row.put("orderNo", orderMap.get(request.getOrderId()) == null ? "-" : orderMap.get(request.getOrderId()).getOrderNo());
            row.put("type", request.getType());
            row.put("reason", request.getReason());
            row.put("status", request.getStatus());
            row.put("reviewComment", request.getReviewComment());
            row.put("createTime", request.getCreateTime());
            payload.add(row);
        }
        return Result.success(payload);
    }

    @PutMapping("/aftersale/{id}/review")
    public Result<String> reviewAfterSale(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        User currentUser = currentUser(authorization);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return Result.error("仅管理员可审核售后");
        }

        AfterSaleRequest request = afterSaleRequestService.getById(id);
        if (request == null) {
            return Result.error("售后记录不存在");
        }
        String status = String.valueOf(body.getOrDefault("status", "REJECTED")).toUpperCase();
        request.setStatus(status);
        request.setReviewComment(String.valueOf(body.getOrDefault("reviewComment", "")));
        request.setUpdateTime(LocalDateTime.now());
        afterSaleRequestService.updateById(request);

        OrderRecord order = orderRecordService.getById(request.getOrderId());
        if (order != null) {
            order.setStatus("APPROVED".equals(status) ? "REFUNDED" : "PAID");
            order.setUpdateTime(LocalDateTime.now());
            orderRecordService.updateById(order);
        }
        OperationLogUtils.record(operationLogService, currentUser, "ORDER", "AFTERSALE_REVIEW", request.getOrderId(), "售后审核为 " + status);
        return Result.success("售后审核完成");
    }

    private User currentUser(String authorization) {
        Long userId = AuthUtils.parseUserId(authorization);
        return userId == null ? null : userService.getById(userId);
    }

    private List<Map<String, Object>> enrichOrders(List<OrderRecord> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> buyerIds = orders.stream().map(OrderRecord::getBuyerId).collect(Collectors.toSet());
        Set<Long> workIds = orders.stream().map(OrderRecord::getWorkId).collect(Collectors.toSet());

        Map<Long, User> userMap = userService.listByIds(buyerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Work> workMap = workService.listByIds(workIds).stream()
                .collect(Collectors.toMap(Work::getId, w -> w, (a, b) -> a));

        List<Map<String, Object>> payload = new ArrayList<>();
        for (OrderRecord order : orders) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", order.getId());
            row.put("orderNo", order.getOrderNo());
            row.put("buyerId", order.getBuyerId());
            row.put("buyerName", userMap.get(order.getBuyerId()) == null ? "-" : userMap.get(order.getBuyerId()).getUsername());
            row.put("workId", order.getWorkId());
            row.put("workTitle", workMap.get(order.getWorkId()) == null ? "-" : workMap.get(order.getWorkId()).getTitle());
            row.put("amount", order.getAmount());
            row.put("status", order.getStatus());
            row.put("remark", order.getRemark());
            row.put("createTime", order.getCreateTime());
            payload.add(row);
        }
        return payload;
    }
}
