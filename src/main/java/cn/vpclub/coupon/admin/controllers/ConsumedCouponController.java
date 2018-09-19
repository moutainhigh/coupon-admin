package cn.vpclub.coupon.admin.controllers;

import cn.vpclub.admin.web.controller.AbstractAdminController;
import cn.vpclub.coupon.admin.rpc.O2oCouponRpcService;
import cn.vpclub.coupon.api.constants.CouponConstant;
import cn.vpclub.coupon.api.requests.o2ocoupon.O2OCouponMixRequest;
import cn.vpclub.coupon.api.utils.JSONUtils;
import cn.vpclub.moses.core.enums.ReturnCodeEnum;
import cn.vpclub.moses.core.model.response.BackResponseUtil;
import cn.vpclub.moses.core.model.response.PageResponse;
import cn.vpclub.moses.utils.common.AccessExcelUtil;
import cn.vpclub.moses.utils.validator.AttributeValidatorException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author:yangqiao
 * @description:
 * @Date:2018/1/15
 */
@RestController
@RequestMapping("/consumedCoupon")
@Slf4j
public class ConsumedCouponController extends AbstractAdminController {

    @Autowired
    private O2oCouponRpcService o2oCouponRpcService;

    //核销列表导出头
    private String[] o2oCouponConsumedExpHeads = {"券码号", "订单编号", "关联商品", "所属商家", "O2O券使用有效期", "核销账号",
            "核销门店", "核销时间", "用户手机号"};

    //核销列表导出头
    private String[] o2oCouponConsumedExpColumns = {"couponCode", "orderNo", "productName", "sellerName", "effectiveDateStr",
            "orgPhone", "orgName", "consumedTimeStr", "buyerPhone"};

    /**
     * 查询O2O券码核销列表
     */
    @PostMapping(value = "/page")
    @ApiOperation("查询卡券核销列表 method")
    @RequiresPermissions("root:coupon:consumedCoupon:page")
    public PageResponse page(@RequestBody O2OCouponMixRequest request, HttpServletRequest servletRequest) {

        log.info("ConsumedCouponController.page: {}", JSONUtils.toJson(request));

        PageResponse response;

        try {
            //设置查询权限，设置当前登录者的所属orgId
            this.setQueryAuthority(request);

            //默认查询已核销
            request.setConsumed(CouponConstant.O2O_COUPON_CONSUMED_Y);

            //业务操作
            response = o2oCouponRpcService.findO2OCouponConsumeStatus(request);
        } catch (Exception e) {
            log.error("error: {}", e);
            response = BackResponseUtil.getPageResponse(ReturnCodeEnum.CODE_1006.getCode());
            response.setMessage(e.getMessage());
        }
        return response;
    }

    /**
     * 导出O2O券码核销列表
     */
    @GetMapping(value = "/export")
    @ApiOperation("导出卡券核销列表 method")
    @RequiresPermissions("root:coupon:consumedCoupon:export")
    public void export(@RequestParam(value = "merchantId", required = false) Long merchantId, @RequestParam(value = "validOrg",
            required = false)
            Long
            validOrg, @RequestParam(value = "productId", required = false) Long productId, @RequestParam(value = "productName",
            required = false) String
                               productName, @RequestParam(value = "buyerPhone", required = false) String
                               buyerPhone, @RequestParam(value = "consumedTimeStart", required = false) Long
                               consumedTimeStart, @RequestParam(value = "consumedTimeEnd", required = false) Long
                               consumedTimeEnd, @RequestParam(value = "consumed", required = false) Integer
                               consumed, HttpServletResponse resp) throws
            IOException, AttributeValidatorException {
        //导出，先设置查询参数
        O2OCouponMixRequest request = new O2OCouponMixRequest();
        request.setMerchantId(merchantId);
        request.setValidOrg(validOrg);
        request.setProductName(productName);
        request.setBuyerPhone(buyerPhone);
        request.setConsumedTimeStart(consumedTimeStart);
        request.setConsumedTimeEnd(consumedTimeEnd);
        request.setConsumed(consumed);
        request.setPageNumber(1);
        request.setPageSize(Integer.MAX_VALUE);
        //设置查询权限，设置当前登录者的所属orgId
        this.setQueryAuthority(request);

        log.info("ConsumedCouponController.export: {}", JSONUtils.toJson(request));
        //查询导出的数据
        PageResponse pageResponse = o2oCouponRpcService.findO2OCouponConsumeStatus(request);

        try {
            resp.setHeader("Content-Disposition", "attachment;filename="
                    + "o2oCouponConsumeStatus.xls");
            AccessExcelUtil.createExcel(resp.getOutputStream(), "o2oCouponConsumeStatus.xls", Arrays.asList(this
                    .o2oCouponConsumedExpHeads), Arrays.asList(this.o2oCouponConsumedExpColumns), pageResponse
                    .getRecords());
            resp.getOutputStream().flush();
        } catch (Exception e) {
            log.error("exportO2OCouponConsumeStatus error:{}", e);
        } finally {
            resp.getOutputStream().close();
        }
    }

    /**
     * 设置查询权限，设置当前登录者的所属orgId
     */
    public void setQueryAuthority(O2OCouponMixRequest request) {

        log.info("setQueryAuthority:{}", (String) super.getLoginUserMap().get("orgId"));
        request.setCurrentUserOrgId(Long.parseLong((String) super.getLoginUserMap().get("orgId")));
    }
}