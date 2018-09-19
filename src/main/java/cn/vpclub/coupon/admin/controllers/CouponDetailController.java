package cn.vpclub.coupon.admin.controllers;

import cn.vpclub.admin.web.controller.AbstractAdminController;
import cn.vpclub.coupon.admin.rpc.CouponDetailRpcService;
import cn.vpclub.coupon.admin.rpc.CouponRpcService;
import cn.vpclub.coupon.api.constants.CouponConstant;
import cn.vpclub.coupon.api.entity.Coupon;
import cn.vpclub.coupon.api.entity.CouponDetail;
import cn.vpclub.coupon.api.requests.coupon.CouponMixRequest;
import cn.vpclub.coupon.api.utils.DateUtils;
import cn.vpclub.coupon.api.utils.JSONUtils;
import cn.vpclub.moses.common.api.commands.product.ReserveProductCommand;
import cn.vpclub.moses.core.enums.ReturnCodeEnum;
import cn.vpclub.moses.core.model.response.BackResponseUtil;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import cn.vpclub.moses.utils.common.AccessExcelUtil;
import cn.vpclub.moses.utils.common.IdWorker;
import cn.vpclub.moses.utils.validator.AttributeValidatorException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author:yangqiao
 * @description:
 * @Date:2018/5/10
 */
@RestController
@RequestMapping("/couponDetail")
@Slf4j
public class CouponDetailController extends AbstractAdminController {

    private CommandGateway commandGateway;

    public CouponDetailController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    private CouponRpcService couponRpcService;

    @Autowired
    private CouponDetailRpcService couponDetailRpcService;

    //卡券详情导出头
    private String[] couponDetailExpHeads = {"卡券批次号", "名称", "关联商品", "卡号", "卡密", "卡券订单号", "所属商家",
            "状态", "发放时间"};

    //卡券详情导出数据
    private String[] couponDetailColumns = {"batchNo", "name", "relatedGoodsName", "cardNo", "cardPwd", "orderNo",
            "relatedBusinessName", "couponDetailStatusDesc", "releaseTimeStr"};

    /**
     * 查询卡券批次
     */
    @PostMapping(value = "/findCouponDetail")
//    @RequiresPermissions("root:coupon:couponDetail:page")
    public PageResponse findCouponDetail(@RequestBody CouponMixRequest request, HttpServletRequest servletRequest) throws
            AttributeValidatorException,
            ParseException,
            IOException {

        log.info("CouponDetailController.findCouponDetail: {}", JSONUtils.toJson(request));

        PageResponse pageResponse = null;

        try {

            //设置当前操作者，如果是企业管理员(admin_qy)或运营管理员(admin_yy)则不设置，可看到全部
            String roleCode = ((String) super.getLoginUserMap().get("roleCode")).trim();
            if (!CouponConstant.ROLECODE_QYGLY.equals(roleCode) && !CouponConstant.ROLECODE_YYGLY.equals(roleCode)) {
                request.setCurrentUserOrgIds((ArrayList<String>) super.getLoginUserMap().get("orgIds"));
            }
            //查询
            pageResponse = couponDetailRpcService.findCouponDetail(request);
        } catch (Exception e) {
            log.error("error: {}", e);
            pageResponse = BackResponseUtil.getPageResponse(ReturnCodeEnum.CODE_1006.getCode());
            pageResponse.setMessage(e.getMessage());
        }

        return pageResponse;
    }

    /**
     * 导出券码详情列表
     */
    @GetMapping(value = "/export")
    @ApiOperation("导出卡券详情列表 method")
//    @RequiresPermissions("root:coupon:couponDetail:export")
    public void export(@RequestParam(value = "appId", required = false) Long appId, @RequestParam(value = "batchNo",
            required = false) String batchNo, @RequestParam(value = "name",
            required = false)
                               String
                               name, @RequestParam(value = "relatedGoodsName", required = false) String relatedGoodsName,
                       @RequestParam(value =
                               "orderNo",
                               required = false) String
                               orderNo, @RequestParam(value = "buyerPhone", required = false) String
                               buyerPhone, @RequestParam(value = "relatedBusiness", required = false) Long
                               relatedBusiness, HttpServletResponse resp) throws
            IOException, AttributeValidatorException {
        //导出，先设置查询参数
        CouponMixRequest request = new CouponMixRequest();
        request.setAppId(appId);
        request.setBatchNo(batchNo);
        request.setName(name);
        request.setRelatedGoodsName(relatedGoodsName);
        request.setBuyerPhone(buyerPhone);
        request.setOrderNo(orderNo);
        request.setRelatedBusiness(relatedBusiness);
        request.setPageNumber(1);
        request.setPageSize(Integer.MAX_VALUE);

        log.info("CouponDetailController.export: {}", JSONUtils.toJson(request));
        //查询导出的数据
        PageResponse pageResponse = couponDetailRpcService.findCouponDetail(request);

        try {
            resp.setHeader("Content-Disposition", "attachment;filename="
                    + "couponDetail.xls");
            AccessExcelUtil.createExcel(resp.getOutputStream(), "couponDetail.xls", Arrays.asList(this
                    .couponDetailExpHeads), Arrays.asList(this.couponDetailColumns), pageResponse
                    .getRecords());
            resp.getOutputStream().flush();
        } catch (Exception e) {
            log.error("CouponDetailController.export error:{}", e);
        } finally {
            resp.getOutputStream().close();
        }
    }

    /**
     * 失效卡券-按批次
     */
    @PostMapping(value = "/loseEfficacyById")
//    @RequiresPermissions("root:coupon:couponDetail:loseEfficacyById")
    public BaseResponse loseEfficacyById(@RequestBody CouponMixRequest couponMixRequest, HttpServletRequest servletRequest)
            throws
            AttributeValidatorException,
            ParseException,
            IOException {

        log.info("CouponDetailController.loseEfficacyById:{}", JSONUtils.toJson(couponMixRequest));

        //校验数据
        if (couponMixRequest == null || couponMixRequest.getAppId() == null || couponMixRequest.getId() == null) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1006.getCode());
        }

        //校验卡券当前状态
        BaseResponse validResponse = this.validCouponDetail(couponMixRequest);
        //如果校验不通过，直接返回
        if (ReturnCodeEnum.CODE_1000.getCode().intValue() == validResponse.getReturnCode().intValue()) {
            return validResponse;
        }

        //组装更新信息
        CouponDetail couponDetail = new CouponDetail();
        couponDetail.setId(couponMixRequest.getId());
        couponDetail.setInvalid(CouponConstant.INVALID_Y);
        //更新人
        couponDetail.setUpdatedBy(Long.parseLong((String) super.getLoginUserMap().get("id")));
        //TODO 修改测试数据
//        couponDetail.setUpdatedBy(123456789L);
        couponDetail.setUpdatedTime(System.currentTimeMillis());
        //失效人
        couponDetail.setInvalidBy(couponDetail.getUpdatedBy());
        couponDetail.setInvalidTime(couponDetail.getUpdatedTime());

        //发送扣减库存命令-按id扣减,扣减单个卡券
        this.sendInventoryMinusCommandById(couponMixRequest.getId());

        //写库
        couponDetailRpcService.update(couponDetail);

        return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());
    }

    /**
     * 校验卡券是否已消费、已失效、已下架、已过期
     */
    private BaseResponse validCouponDetail(CouponMixRequest couponMixRequest) {

        BaseResponse baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());

        PageResponse pageResponse = couponDetailRpcService.findCouponDetail(couponMixRequest);

        //获取卡密信息，校验数据
        if (pageResponse != null && CollectionUtils.isNotEmpty(pageResponse.getRecords())) {

            CouponMixRequest result = (CouponMixRequest) pageResponse.getRecords().get(0);
            //是否下架
            if (CouponConstant.OFF_STATUS_Y == result.getOffStatus()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券已下架，不能失效");
            }
            //是否过期
            if (result.getEffectiveDateEnd() < System.currentTimeMillis()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券已过期，不能失效");
            }
            //是否已消费
            if (CouponConstant.ALLOCATION_STATUS_Y == result.getAllocationStatus()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券已消费，不能失效");
            }
            //是否已失效
            if (CouponConstant.INVALID_Y == result.getInvalid()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券已失效，不能失效");
            }
        } else {

            baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1002.getCode());
            baseResponse.setMessage("卡券信息不存在");
        }

        return baseResponse;
    }

    /**
     * 发送扣减库存命令-按id扣减,扣减单个卡券
     */
    private void sendInventoryMinusCommandById(Long id) {
        //验证数据
        if (id == null) {
            return;
        }

        log.info("开始，发送扣减库存命令id:{}", id);

        //查询单个卡券对应sku信息
        CouponMixRequest couponMixRequest = new CouponMixRequest();
        couponMixRequest.setId(id);
        BaseResponse baseResponse = couponDetailRpcService.findCouponDetailToLoseEfficacy(couponMixRequest);
        //如果有数据，则发送命令
        if (baseResponse.getReturnCode().intValue() == ReturnCodeEnum.CODE_1000.getCode().intValue()) {

            //查询的信息
            List<CouponMixRequest> couponMixRequestList = (List<CouponMixRequest>) baseResponse.getDataInfo();

            if (CollectionUtils.isNotEmpty(couponMixRequestList)) {

                //扣减库存命令
                ReserveProductCommand reserveProductCommand = new ReserveProductCommand();
                //生成一个随机的订单id
                reserveProductCommand.setOrderId(IdWorker.getId());
                //sku
                reserveProductCommand.setSkuId(couponMixRequestList.get(0).getRelatedGoodsSku());
                //增加库存数量
                reserveProductCommand.setNumber(1L);

                log.info("sendInventoryMinusCommandById.reserveProductCommand:{}", JSONUtils.toJson(reserveProductCommand));
                try {
                    //发送命令
                    commandGateway.send(reserveProductCommand);
                } catch (CommandExecutionException e) {
                    log.error("reserveProductCommand CommandExecutionException error:{}", e);
                    throw e;
                } catch (Exception e) {
                    log.error("reserveProductCommand Exception error:{}", e);
                    throw e;
                }
            }
        }

        log.info("结束，发送扣减库存命令id:{}", id);
    }

    /**
     * 失效卡券-按批次
     */
    @PostMapping(value = "/loseEfficacyByBatch")
//    @RequiresPermissions("root:coupon:couponDetail:loseEfficacyByBatch")
    public BaseResponse loseEfficacyByBatch(@RequestBody CouponMixRequest couponMixRequest, HttpServletRequest servletRequest)
            throws
            AttributeValidatorException,
            ParseException,
            IOException {

        log.info("CouponDetailController.loseEfficacyByBatch:{}", JSONUtils.toJson(couponMixRequest));

        //校验数据
        if (couponMixRequest == null || couponMixRequest.getAppId() == null || StringUtils.isEmpty
                (couponMixRequest.getCouponTypeBatchNo())) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1006.getCode());
        }

        //校验卡券批次当前状态
        BaseResponse validResponse = this.validCouponBatch(couponMixRequest);
        //如果校验不通过，直接返回
        if (ReturnCodeEnum.CODE_1000.getCode().intValue() == validResponse.getReturnCode().intValue()) {
            return validResponse;
        }

        //发送扣减库存命令-按批次扣减,扣减此批次号内所有剩余库存
        this.sendInventoryMinusCommandByBatch(couponMixRequest.getCouponTypeBatchNo(), couponMixRequest.getAppId());

        //更新卡券明细信息
        this.loseDetailEfficacy(couponMixRequest);

        //更新卡券批次信息
        this.loseBatchEfficacy(couponMixRequest);

        return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());
    }

    /**
     * 校验卡券批次当前状态
     */
    private BaseResponse validCouponBatch(CouponMixRequest couponMixRequest) {

        BaseResponse baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());

        PageResponse pageResponse = couponRpcService.findCouponBatch(couponMixRequest);

        //获取卡密信息，校验数据
        if (pageResponse != null && CollectionUtils.isNotEmpty(pageResponse.getRecords())) {

            CouponMixRequest result = (CouponMixRequest) pageResponse.getRecords().get(0);
            //是否下架
            if (CouponConstant.OFF_STATUS_Y == result.getOffStatus()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券批次已下架，不能失效");
            }
            //是否过期
            if (result.getEffectiveDateEnd() < System.currentTimeMillis()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券批次已过期，不能失效");
            }
            //是否已消费
            if (CouponConstant.ALLOCATION_STATUS_Y == result.getAllocationStatus()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券批次已消费，不能失效");
            }
            //是否已失效
            if (CouponConstant.INVALID_Y == result.getInvalid()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券批次已失效，不能失效");
            }
        } else {

            baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1002.getCode());
            baseResponse.setMessage("卡券批次信息不存在");
        }

        return baseResponse;
    }

    /**
     * 失效卡券批次信息
     */
    private void loseBatchEfficacy(CouponMixRequest couponMixRequest) {
        //组装更新数据
        Coupon coupon = new Coupon();
        //批次号
        coupon.setBatchNo(couponMixRequest.getCouponTypeBatchNo());
        //appId
        coupon.setAppId(couponMixRequest.getAppId());
        //更新人
        coupon.setUpdatedBy(Long.parseLong((String) super.getLoginUserMap().get("id")));
        //TODO 修改测试数据
//        coupon.setUpdatedBy(123456789L);
        //失效人
        coupon.setInvalidBy(coupon.getUpdatedBy());

        couponRpcService.loseBatchEfficacy(coupon);
    }

    /**
     * 失效卡券明细信息
     */
    private void loseDetailEfficacy(CouponMixRequest couponMixRequest) {
        //组装更新数据
        CouponDetail couponDetail = new CouponDetail();
        //批次号
        couponDetail.setCouponTypeBatchNo(couponMixRequest.getCouponTypeBatchNo());
        //更新人
        couponDetail.setUpdatedBy(Long.parseLong((String) super.getLoginUserMap().get("id")));
        //TODO 修改测试数据
//        couponDetail.setUpdatedBy(123456789L);
        //失效人
        couponDetail.setInvalidBy(couponDetail.getUpdatedBy());

        couponDetailRpcService.loseEfficacyByBatch(couponDetail);
    }


    /**
     * 发送扣减库存命令-按批次扣减,扣减此批次号内所有剩余库存
     */
    private void sendInventoryMinusCommandByBatch(String batchNo, Long appId) {

        //验证数据
        if (StringUtils.isEmpty(batchNo)) {
            return;
        }

        log.info("开始，发送扣减库存命令批次号:{}", batchNo);

        //查询卡批次对应sku信息
        CouponMixRequest couponMixRequest = new CouponMixRequest();
        couponMixRequest.setBatchNo(batchNo);
        couponMixRequest.setAppId(appId);
        BaseResponse baseResponse = couponDetailRpcService.findCouponDetailToLoseEfficacy(couponMixRequest);
        //如果有数据，则发送命令
        if (baseResponse.getReturnCode().intValue() == ReturnCodeEnum.CODE_1000.getCode().intValue()) {

            //查询的信息
            List<CouponMixRequest> couponMixRequestList = (List<CouponMixRequest>) baseResponse.getDataInfo();

            if (CollectionUtils.isNotEmpty(couponMixRequestList)) {

                //扣减库存命令
                ReserveProductCommand reserveProductCommand = new ReserveProductCommand();
                //生成一个随机的订单id
                reserveProductCommand.setOrderId(IdWorker.getId());
                //sku
                reserveProductCommand.setSkuId(couponMixRequestList.get(0).getRelatedGoodsSku());
                //增加库存数量
                reserveProductCommand.setNumber(new Long((long) couponMixRequestList.size()));

                log.info("reserveProductCommand:{}", JSONUtils.toJson(reserveProductCommand));

                try {
                    //发送命令
                    commandGateway.send(reserveProductCommand);
                } catch (CommandExecutionException e) {
                    log.error("reserveProductCommand CommandExecutionException error:{}", e);
                    throw e;
                } catch (Exception e) {
                    log.error("reserveProductCommand Exception error:{}", e);
                    throw e;
                }
            }
        }

        log.info("结束，发送扣减库存命令批次号:{}", batchNo);
    }

    /**
     * 查询下架日期到期的卡券，修改状态，给商品模块发送扣减库存命令
     * <p>
     * 每天凌晨执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void loseEfficacyOffDateExpires() throws ParseException {

        log.info("开始，查询下架日期到期的卡券，修改状态，给商品模块发送扣减库存命令");

        //得到今天23是59分59秒的时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long currentDateEndTime = DateUtils.getEndTimeByDate(sdf.format(new Date()));

        //组装查询参数，查询下架日期小于当前日期且下架状态为未下架的卡批次
        CouponMixRequest param = new CouponMixRequest();
        param.setOffDate(currentDateEndTime);

        //查询卡券批次信息
        PageResponse couponBatchResponse = couponRpcService.findCouponBatchOffDateExpires(param);
        if (couponBatchResponse.getReturnCode().intValue() == ReturnCodeEnum.CODE_1000.getCode().intValue()) {
            List<CouponMixRequest> couponMixRequestList = (List<CouponMixRequest>) couponBatchResponse.getRecords();
            //如果有需要处理的信息，则执行下架逻辑
            if (CollectionUtils.isNotEmpty(couponMixRequestList)) {

                log.info("本次下架批次数:{}", couponMixRequestList.size());

                for (CouponMixRequest couponMixRequest : couponMixRequestList) {

                    log.info("本次下架批次号:{}", couponMixRequest.getBatchNo());

                    //发送扣减库存命令
                    this.sendInventoryMinusCommandByBatch(couponMixRequest.getBatchNo(), couponMixRequest.getAppId());

                    //修改卡券批次状态为已下架
                    Coupon coupon = new Coupon();
                    coupon.setId(couponMixRequest.getId());
                    coupon.setOffStatus(CouponConstant.OFF_STATUS_Y);
                    couponRpcService.update(coupon);
                }
            }
        }


        log.info("结束，查询下架日期到期的卡券，修改状态，给商品模块发送扣减库存命令");
    }

    /**
     * 重新发送短信接口Kafka
     */
    @PostMapping(value = "/reSendMsg")
    @ApiOperation("发送短信接口")
//    @RequiresPermissions("root:coupon:couponDetail:reSendMsg")
    public BaseResponse reSendMsg(@RequestBody CouponMixRequest request, HttpServletRequest
            servletRequest) {

        //校验订单id是否为空
        if (request == null || request.getAppId() == null || request.getSubOrderId() == null) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1006.getCode());
        }

        return couponRpcService.reSendMsg(request);
    }
}