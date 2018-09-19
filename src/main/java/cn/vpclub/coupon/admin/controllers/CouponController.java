package cn.vpclub.coupon.admin.controllers;

import cn.vpclub.admin.web.controller.AbstractAdminController;
import cn.vpclub.coupon.admin.rpc.O2oCouponRpcService;
import cn.vpclub.coupon.admin.rpc.OrderPaidEventLogRpcService;
import cn.vpclub.coupon.admin.rpc.ThirdPartyCouponLogRpcService;
import cn.vpclub.coupon.api.constants.CouponConstant;
import cn.vpclub.coupon.api.entity.O2oCoupon;
import cn.vpclub.coupon.api.entity.OrderPaidEventLog;
import cn.vpclub.coupon.api.entity.ThirdPartyCouponLog;
import cn.vpclub.coupon.api.requests.order.OrderRequest;
import cn.vpclub.coupon.api.utils.JSONUtils;
import cn.vpclub.moses.core.enums.ReturnCodeEnum;
import cn.vpclub.moses.core.model.response.BackResponseUtil;
import cn.vpclub.moses.core.model.response.BaseResponse;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author:yangqiao
 * @description:卡券后台管理
 * @Date:2017/12/13
 */
@RestController
@RequestMapping("/coupon")
@Slf4j
public class CouponController extends AbstractAdminController {

    @Autowired
    private ThirdPartyCouponLogRpcService thirdPartyCouponLogRpcService;

    @Autowired
    private O2oCouponRpcService o2oCouponRpcService;

    @Autowired
    private OrderPaidEventLogRpcService orderPaidEventLogRpcService;

    /**
     * 根据订单信息查询其包含的卡券或券码信息
     */
    @PostMapping(value = "/queryCouponByOrder")
    @ApiOperation("根据订单信息查询其包含的卡券或券码信息 method")
    @RequiresPermissions("root:coupon:coupon:queryCouponByOrder")
    public BaseResponse queryCouponByOrder(@RequestBody OrderRequest request, HttpServletRequest servletRequest) {

        log.info("request:{}", JSONUtils.toJson(request));

        //先判断是否卡券订单
        OrderPaidEventLog orderPaidEventLog = new OrderPaidEventLog();
        orderPaidEventLog.setSubOrderId(request.getOrderId());
        BaseResponse orderLogResponse = orderPaidEventLogRpcService.query(orderPaidEventLog);
        if (ReturnCodeEnum.CODE_1000.getCode().intValue() != orderLogResponse.getReturnCode().intValue()) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1002.getCode());
        }

        //返回对象
        OrderRequest result = new OrderRequest();

        //先查询第三方服务调用日志表
        ThirdPartyCouponLog parms = new ThirdPartyCouponLog();
        parms.setSubOrderId(request.getOrderId());

        BaseResponse response = thirdPartyCouponLogRpcService.query(parms);
        //判断是否是第三方发券
        if (ReturnCodeEnum.CODE_1000.getCode().intValue() == response.getReturnCode().intValue()) {
            ThirdPartyCouponLog thirdPartyCouponLog = (ThirdPartyCouponLog) response.getDataInfo();
            //发券方
            result.setCouponSource(thirdPartyCouponLog.getServiceParty());
            //第三方返回标识
            result.setThirdReturnCode(thirdPartyCouponLog.getReturnCode());//1是成功，2是失败
            //第三方返回描述信息
            result.setThirdReturnCodeDesc(CouponConstant.THIRD_PARTY_SERVICE_FLAG_SUCCESS == thirdPartyCouponLog.getReturnCode
                    () ? CouponConstant.THIRD_PARTY_SERVICE_FLAG_SUCCESS_DESC : CouponConstant
                    .THIRD_PARTY_SERVICE_FLAG_FAILED_DESC);

        }
        //平台发券
        else {
            //平台发券
            result.setCouponSource(CouponConstant.SERVICE_PARTY_PLATFORM);

            O2oCoupon o2oCoupon = new O2oCoupon();
            o2oCoupon.setAppId(request.getAppId());
            o2oCoupon.setOrderId(request.getOrderId());

            response = o2oCouponRpcService.findList(o2oCoupon);

            //如果不为空
            if (ReturnCodeEnum.CODE_1000.getCode().intValue() == response.getReturnCode().intValue()) {
                List<O2oCoupon> o2oCouponList = (List<O2oCoupon>) response.getDataInfo();
                if (CollectionUtils.isNotEmpty(o2oCouponList)) {
                    result.setO2oCouponList(o2oCouponList);
                }
            }
        }

        response.setDataInfo(result);


        return response;

    }
}