package cn.vpclub.coupon.admin.controllers;

import cn.vpclub.admin.web.controller.AbstractAdminController;
import cn.vpclub.coupon.admin.rpc.CouponRpcService;
import cn.vpclub.coupon.admin.rpc.OrderPaidEventLogRpcService;
import cn.vpclub.coupon.api.commands.other.CouponResendCommand;
import cn.vpclub.coupon.api.entity.OrderPaidEventLog;
import cn.vpclub.coupon.api.events.coupon.ResendCouponEvent;
import cn.vpclub.coupon.api.requests.coupon.CouponLog;
import cn.vpclub.coupon.api.utils.JSONUtils;
import cn.vpclub.moses.common.api.events.pay.OrderPaidEvent;
import cn.vpclub.moses.core.enums.ReturnCodeEnum;
import cn.vpclub.moses.core.model.response.BackResponseUtil;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import cn.vpclub.moses.utils.common.IdWorker;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author:yangqiao
 * @description:卡券后台管理
 * @Date:2017/12/13
 */
@RestController
@RequestMapping("/couponLog")
@Slf4j
public class CouponLogController extends AbstractAdminController {

    private CommandGateway commandGateway;

    public CouponLogController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    private OrderPaidEventLogRpcService orderPaidEventLogRpcService;

    @Autowired
    private CouponRpcService couponRpcService;

    /**
     * 券码发放状态
     */
    @PostMapping(value = "/page")
    @ApiOperation("查询卡券发放日志 method")
//    @RequiresPermissions("root:coupon:couponLog:page")
    public PageResponse page(@RequestBody CouponLog request, HttpServletRequest servletRequest) {
        return couponRpcService.findCouponLog(request);
    }

    /**
     * 重发卡券
     */
    @PostMapping(value = "/resendCoupon")
    @ApiOperation("重发卡券 method")
    @RequiresPermissions("root:coupon:couponLog:resendCoupon")
    public BaseResponse resendCoupon(@RequestBody CouponLog request, HttpServletRequest servletRequest) {
        if (request == null || request.getOrderId() == null) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1006.getCode());
        }

        log.error("resendCoupon request: {}", JSONUtils.toJson(request));

        //先查询订单支付日志
        OrderPaidEventLog eventLog = new OrderPaidEventLog();
        eventLog.setMainOrderId(request.getOrderId());
        BaseResponse eventLogResponse = orderPaidEventLogRpcService.query(eventLog);
        //如果查询记录为空，则直接返回
        if (eventLogResponse.getReturnCode().intValue() != ReturnCodeEnum.CODE_1000.getCode().intValue()) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1002.getCode());
        }
        eventLog = (OrderPaidEventLog) eventLogResponse.getDataInfo();

        //获得最初订单模块发送的订单支付事件
        OrderPaidEvent orderPaidEvent = JSONUtils.toObject(eventLog.getOrderPaidInfo(), OrderPaidEvent.class);
        ResendCouponEvent resendCouponEvent = new ResendCouponEvent(orderPaidEvent);

        CouponResendCommand command = new CouponResendCommand(IdWorker.getId(), resendCouponEvent);

        try {
            commandGateway.sendAndWait(command);
        } catch (Exception e) {
            log.error("resendCoupon: {}", e);
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
        }

        //返回操作成功
        return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());
    }

}