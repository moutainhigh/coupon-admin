package cn.vpclub.coupon.admin.controllers;

import cn.vpclub.admin.web.controller.AbstractAdminController;
import cn.vpclub.coupon.admin.rpc.CouponDetailRpcService;
import cn.vpclub.coupon.admin.rpc.CouponRpcService;
import cn.vpclub.coupon.api.commands.coupon.CreateCouponBatchCommand;
import cn.vpclub.coupon.api.commands.coupon.CreateCouponDetailCommand;
import cn.vpclub.coupon.api.constants.CouponConstant;
import cn.vpclub.coupon.api.entity.Coupon;
import cn.vpclub.coupon.api.entity.CouponDetail;
import cn.vpclub.coupon.api.requests.coupon.CouponMixRequest;
import cn.vpclub.coupon.api.utils.JSONUtils;
import cn.vpclub.moses.common.api.commands.product.RollbackReservationCommand;
import cn.vpclub.moses.core.enums.ReturnCodeEnum;
import cn.vpclub.moses.core.model.response.BackResponseUtil;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import cn.vpclub.moses.utils.common.AccessExcelUtil;
import cn.vpclub.moses.utils.common.IdWorker;
import cn.vpclub.moses.utils.constant.ValidatorConditionType;
import cn.vpclub.moses.utils.validator.AttributeValidatorException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:yangqiao
 * @description:
 * @Date:2018/3/27
 */
@RestController
@RequestMapping("/couponBatch")
@Slf4j
public class CouponBatchController extends AbstractAdminController {

    private CommandGateway commandGateway;

    public CouponBatchController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    private CouponRpcService couponRpcService;

    @Autowired
    private CouponDetailRpcService couponDetailRpcService;

    /**
     * 查询卡券批次
     */
    @PostMapping(value = "/findCouponBatch")
//    @RequiresPermissions("root:coupon:couponBatch:page")
    public PageResponse findCouponBatch(@RequestBody CouponMixRequest request, HttpServletRequest servletRequest) throws
            AttributeValidatorException,
            ParseException,
            IOException {

        log.info("CouponBatchController.findCouponBatch: {}", JSONUtils.toJson(request));

        PageResponse pageResponse = null;

        try {

            //设置当前操作者，如果是企业管理员(admin_qy)或运营管理员(admin_yy)则不设置，可看到全部
            String roleCode = ((String) super.getLoginUserMap().get("roleCode")).trim();
            if (!CouponConstant.ROLECODE_QYGLY.equals(roleCode) && !CouponConstant.ROLECODE_YYGLY.equals(roleCode)) {
                request.setCurrentUserOrgIds((ArrayList<String>) super.getLoginUserMap().get("orgIds"));
            }
            //查询
            pageResponse = couponRpcService.findCouponBatch(request);
        } catch (Exception e) {
            log.error("error: {}", e);
            pageResponse = BackResponseUtil.getPageResponse(ReturnCodeEnum.CODE_1006.getCode());
            pageResponse.setMessage(e.getMessage());
        }

        return pageResponse;
    }

    /**
     * 新增卡券批次
     */
    @PostMapping(value = "/add")
//    @RequiresPermissions("root:coupon:couponBatch:add")
    public BaseResponse add(@RequestBody CouponMixRequest request, HttpServletRequest servletRequest) throws
            AttributeValidatorException, ParseException {

        log.info("CouponBatchController.add:{}", JSONUtils.toJson(request));

        //校验参数
        BaseResponse result = this.validAddData(request);
        //如果不是1000，则返回错误信息
        if (result.getReturnCode().intValue() != ReturnCodeEnum.CODE_1000.getCode().intValue()) {
            return result;
        }

        //返回信息
        BaseResponse baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());

        //新增卡券批次
        baseResponse.setDataInfo(this.addBatch(request));

        return baseResponse;
    }

    /**
     * 下载导入模板
     */
    @GetMapping(value = "/downloadTemplate")
    @ApiOperation("下载导入模板 method")
//    @RequiresPermissions("root:coupon:couponBatch:downloadTemplate")
    public ResponseEntity<FileSystemResource> downloadTemplate() throws IOException, AttributeValidatorException {

        File file = ResourceUtils.getFile("classpath:template/卡密导入模板.xlsx");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", URLEncoder.encode(file.getName(),
                "UTF-8")));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream; charset=utf-8"))
                .body(new FileSystemResource(file));
    }

    /**
     * 导入卡券
     */
    @PostMapping(value = "/importCoupon")
//    @RequiresPermissions("root:coupon:couponBatch:import")
    public BaseResponse importCoupon(@RequestParam MultipartFile fileData, String batchNo, Long appId) throws
            AttributeValidatorException,
            ParseException,
            IOException {

        BaseResponse baseResponse = null;

        //校验数据
        if (fileData == null) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1006.getCode());
        }

        //解析excel
        List<CouponDetail> couponDetailList = this.parseExcel(fileData);

        //验证导入数据
        PageResponse validResponse = this.validImportData(couponDetailList, batchNo, appId);
        //如果查询出有重复数据，则返回
        if (validResponse.getReturnCode().intValue() == ReturnCodeEnum.CODE_1000.getCode().intValue()) {
            baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1008.getCode());
            baseResponse.setMessage("卡号、密码重复");

            return baseResponse;
        }

        //写库
        log.info("开始导入卡券写库动作,{}", JSONUtils.toJson(couponDetailList));
        baseResponse = this.addCouponDetail(couponDetailList, batchNo, appId);

        //如果写库不成功，则直接返回，不发送增加库存命令
        if (baseResponse.getReturnCode().intValue() != ReturnCodeEnum.CODE_1000.getCode().intValue()) {

            return baseResponse;
        }

        //发送增加库存命令
        this.sendInventoryAddCommand(batchNo, couponDetailList);

        //返回本次导入的数据
        baseResponse.setDataInfo(couponDetailList);

        return baseResponse;
    }

    /**
     * 发送增加库存命令
     */
    private void sendInventoryAddCommand(String batchNo, List<CouponDetail> couponDetailList) {

        //验证数据
        if (StringUtils.isEmpty(batchNo) || CollectionUtils.isEmpty(couponDetailList)) {
            return;
        }

        //查询卡批次对应sku信息
        Coupon coupon = new Coupon();
        coupon.setBatchNo(batchNo);
        BaseResponse baseResponse = couponRpcService.query(coupon);
        //如果有数据，则发送命令
        if (baseResponse.getReturnCode().intValue() == ReturnCodeEnum.CODE_1000.getCode().intValue()) {

            //增加库存命令
            RollbackReservationCommand rollbackReservationCommand = new RollbackReservationCommand();
            //生成一个随机的订单id
            rollbackReservationCommand.setOrderId(IdWorker.getId());
            //sku
            rollbackReservationCommand.setSkuId(((Coupon) baseResponse.getDataInfo()).getRelatedGoodsSku());
            //增加库存数量
            rollbackReservationCommand.setNumber(new Long((long) couponDetailList.size()));

            //发送命令
            try {

                log.info("rollbackReservationCommand:{}", JSONUtils.toJson(rollbackReservationCommand));

                commandGateway.send(rollbackReservationCommand);
            } catch (CommandExecutionException e) {
                log.error("rollbackReservationCommand CommandExecutionException error:{}", e);
                throw e;
            } catch (Exception e) {
                log.error("rollbackReservationCommand Exception error:{}", e);
                throw e;
            }
        }
    }

    /**
     * 修改卡券批次信息
     */
    @PostMapping(value = "/updateBatch")
//    @RequiresPermissions("root:coupon:couponBatch:update")
    public BaseResponse updateBatch(@RequestBody CouponMixRequest couponMixRequest, HttpServletRequest servletRequest) throws
            AttributeValidatorException,
            ParseException,
            IOException {

        log.info("CouponBatchController.updateBatch:{}", JSONUtils.toJson(couponMixRequest));

        BaseResponse validResponse = this.validUpdateData(couponMixRequest);

        //如果不是1000，则返回错误信息
        if (validResponse.getReturnCode().intValue() != ReturnCodeEnum.CODE_1000.getCode().intValue()) {
            return validResponse;
        }

        //组装更新数据
        Coupon coupon = new Coupon();
        //id
        coupon.setId(couponMixRequest.getId());
        //名称
        coupon.setName(couponMixRequest.getName());
        //卡券使用有效期起始日期
        if (couponMixRequest.getEffectiveDateStart() != null) {
            coupon.setEffectiveDateStart(couponMixRequest.getEffectiveDateStart());
        }
        //卡券使用有效期终止日期
        if (couponMixRequest.getEffectiveDateEnd() != null) {
            coupon.setEffectiveDateEnd(couponMixRequest.getEffectiveDateEnd());
        }
        //下架日期
        if (couponMixRequest.getOffDate() != null) {
            coupon.setOffDate(couponMixRequest.getOffDate());
        }

        //更新卡密信息
        this.updateCouponDetail(couponMixRequest);

        //写库
        return this.couponRpcService.update(coupon);
    }

    /**
     * 校验更新信息
     */
    private BaseResponse validUpdateData(CouponMixRequest couponMixRequest) throws ParseException {

        //校验数据
        if (couponMixRequest == null || couponMixRequest.getId() == null || StringUtils.isEmpty(couponMixRequest.getBatchNo()) ||
                StringUtils.isEmpty(couponMixRequest.getName())) {
            return BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1006.getCode());
        }

        //返回的校验信息
        BaseResponse returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());

        //校验卡券批次当前状态
        BaseResponse validResponse = this.validCouponBatch(couponMixRequest);
        //如果校验不通过，直接返回
        if (ReturnCodeEnum.CODE_1000.getCode().intValue() == validResponse.getReturnCode().intValue()) {
            return validResponse;
        }

        //校验日期参数
        //失效日期不能小于生效日期
        if (couponMixRequest.getEffectiveDateEnd() < couponMixRequest.getEffectiveDateStart()) {
            returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
            returnResponse.setMessage("失效日期不能小于生效日期");

            return returnResponse;
        }
        //下架日期需大于生效日期，小于失效日期
        if ((couponMixRequest.getOffDate() <= couponMixRequest.getEffectiveDateStart()) || (couponMixRequest.getOffDate() >=
                couponMixRequest.getEffectiveDateEnd())) {
            returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
            returnResponse.setMessage("下架日期需大于生效日期，小于失效日期");

            return returnResponse;
        }

        return returnResponse;
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
                baseResponse.setMessage("卡券批次已下架，不能修改");
            }
            //是否过期
            if (result.getEffectiveDateEnd() < System.currentTimeMillis()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券批次已过期，不能修改");
            }
            //是否已消费
            if (CouponConstant.ALLOCATION_STATUS_Y == result.getAllocationStatus()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券批次已消费，不能修改");
            }
            //是否已失效
            if (CouponConstant.INVALID_Y == result.getInvalid()) {

                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1004.getCode());
                baseResponse.setMessage("卡券批次已失效，不能修改");
            }
        } else {

            baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1002.getCode());
            baseResponse.setMessage("卡券批次信息不存在");
        }

        return baseResponse;
    }

    /**
     * 更新卡密信息
     */
    private void updateCouponDetail(CouponMixRequest couponMixRequest) {

        //组装更新卡密信息参数
        CouponMixRequest param = new CouponMixRequest();
        param.setCouponTypeBatchNo(couponMixRequest.getBatchNo());
        param.setEffectiveDateStart(couponMixRequest.getEffectiveDateStart());
        param.setEffectiveDateEnd(couponMixRequest.getEffectiveDateEnd());

        couponDetailRpcService.updateModel(param);
    }

    /**
     * 卡密明细写库
     */
    private BaseResponse addCouponDetail(List<CouponDetail> couponDetailList, String batchNo, Long appId) {

        BaseResponse baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());
        //组装写库参数
        if (CollectionUtils.isNotEmpty(couponDetailList)) {
            //查询对应批次信息
            Coupon couponParm = new Coupon();
            couponParm.setBatchNo(batchNo);
            couponParm.setAppId(appId);
            couponParm.setDeleted(CouponConstant.DELETED_N);
            BaseResponse batchResponse = couponRpcService.query(couponParm);
            //判断导入的卡密是否已存在
            if (batchResponse.getReturnCode().intValue() != ReturnCodeEnum.CODE_1000.getCode().intValue()) {
                baseResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
                baseResponse.setMessage(MessageFormat.format("卡券批次号{0}不存在", batchNo));

                log.error(baseResponse.getMessage());

                return baseResponse;
            }

            Coupon couponBatch = (Coupon) batchResponse.getDataInfo();

            for (CouponDetail param : couponDetailList) {
                param.setId(IdWorker.getId());
                param.setAppId(appId);
                param.setCouponTypeBatchNo(batchNo);
                //TODO 修改测试数据
                param.setCreatedBy(Long.parseLong((String) super.getLoginUserMap().get("id")));
//                param.setCreatedBy(123456789L);
                param.setCreatedTime(System.currentTimeMillis());
                param.setUpdatedBy(param.getCreatedBy());
                param.setUpdatedTime(param.getCreatedTime());
                param.setDeleted(CouponConstant.DELETED_N);
                param.setAllocationStatus(CouponConstant.ALLOCATION_STATUS_N);
                param.setResendMsgStatus(CouponConstant.RESENDMSG_STATUS_N);
                param.setInvalid(CouponConstant.INVALID_N);
                param.setEffectiveDateStart(couponBatch.getEffectiveDateStart());
                param.setEffectiveDateEnd(couponBatch.getEffectiveDateEnd());
            }

            //发送创建卡密命令
            CreateCouponDetailCommand command = new CreateCouponDetailCommand();
            command.setId(IdWorker.getId());
            command.setCouponTypeBatchNo(couponBatch.getBatchNo());
            command.setRelatedGoodsSku(couponBatch.getRelatedGoodsSku());
            command.setCouponDetailList(couponDetailList);

            commandGateway.send(command);
        }


        return baseResponse;
    }

    /**
     * 验证导入数据
     */
    private PageResponse validImportData(List<CouponDetail> couponDetailList, String batchNo, Long appId) {

        BaseResponse baseResponse = null;
        //验证数据
        if (CollectionUtils.isNotEmpty(couponDetailList)) {
            for (CouponDetail param : couponDetailList) {
                //卡号，密码不能为空
                if (StringUtils.isEmpty(param.getCardNo()) || StringUtils.isEmpty(param.getCardPwd())) {
                    return BackResponseUtil.getPageResponse(ReturnCodeEnum.CODE_1006.getCode());
                }
                param.setCouponTypeBatchNo(batchNo);
                param.setAppId(appId);
            }
        }

        CouponMixRequest request = new CouponMixRequest();
        request.setRecords(couponDetailList);

        return couponDetailRpcService.validRepeatData(request);

    }

    /**
     * 解析excel
     */
    private List<CouponDetail> parseExcel(MultipartFile file) throws IOException {

        //需解析的字段集合
        List<String> columnList = new ArrayList<String>();
        columnList.add("cardNo");
        columnList.add("cardPwd");

        return AccessExcelUtil.parseExcel(file.getInputStream(), CouponDetail.class, columnList,
                1, 0, 0);
    }

    /**
     * 新增卡券批次-写库
     */
    private Coupon addBatch(CouponMixRequest request) throws ParseException {
        Coupon coupon = new Coupon();
        coupon.setId(IdWorker.getId());
        coupon.setName(request.getName());
        coupon.setAppId(request.getAppId());
        coupon.setDeleted(CouponConstant.DELETED_N);
        coupon.setBatchNo(String.valueOf(IdWorker.getId()).substring(9));
        coupon.setRelatedBusiness(request.getRelatedBusiness());
        coupon.setRelatedBusinessName(request.getRelatedBusinessName());

        coupon.setRelatedGoods(request.getRelatedGoods());
        coupon.setRelatedGoodsSku(request.getRelatedGoodsSku());
        coupon.setRelatedGoodsName(request.getRelatedGoodsName());

        coupon.setEffectiveDateStart(request.getEffectiveDateStart());
        coupon.setEffectiveDateEnd(request.getEffectiveDateEnd());
        coupon.setOffDate(request.getOffDate());
        coupon.setOffStatus(CouponConstant.OFF_STATUS_N);
        coupon.setCreatedBy(Long.parseLong((String) super.getLoginUserMap().get("id")));
//        coupon.setCreatedBy(123456789L);
        coupon.setCreatedTime(System.currentTimeMillis());
        coupon.setUpdatedBy(coupon.getCreatedBy());
        coupon.setUpdatedTime(coupon.getCreatedTime());
        coupon.setInvalid(CouponConstant.INVALID_N);
        coupon.setInventory(0);

        CreateCouponBatchCommand command = new CreateCouponBatchCommand();
        command.setId(IdWorker.getId());
        command.setCoupon(coupon);

        commandGateway.send(command);

        return coupon;
    }

    /**
     * 校验新增卡券批次数据
     */
    private BaseResponse validAddData(CouponMixRequest request) throws AttributeValidatorException, ParseException {
        //校验参数
        request.validate(ValidatorConditionType.CREATE);

        //返回值
        BaseResponse returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1000.getCode());

        //校验日期参数
        //失效日期不能小于生效日期
        if (request.getEffectiveDateEnd() < request.getEffectiveDateStart()) {
            returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
            returnResponse.setMessage("失效日期不能小于生效日期");

            return returnResponse;
        }
        //下架日期需大于生效日期，小于失效日期
        if ((request.getOffDate() <= request.getEffectiveDateStart()) || (request.getOffDate() >= request.getEffectiveDateEnd()
        )) {
            returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
            returnResponse.setMessage("下架日期需大于生效日期，小于失效日期");

            return returnResponse;
        }

        //查询参数
        Coupon param = null;

        //查询值
        BaseResponse baseResponse = null;

        //查询选择商品是否已关联卡券
        param = new Coupon();
        param.setRelatedGoodsSku(request.getRelatedGoodsSku());
        param.setDeleted(CouponConstant.DELETED_N);
        param.setOffStatus(CouponConstant.OFF_STATUS_N);
        param.setInvalid(CouponConstant.INVALID_N);
        //查询数据库
        baseResponse = couponRpcService.query(param);
        //如果已关联卡券，则返回错误信息
        if (baseResponse.getReturnCode().intValue() == ReturnCodeEnum.CODE_1000.getCode().intValue()) {
            returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
            returnResponse.setMessage("此商品已关联卡券，不可关联多个卡券");
        }

        //查询名称是否重复
        param = new Coupon();
        param.setName(request.getName());
        param.setDeleted(CouponConstant.DELETED_N);
        //查询数据库
        baseResponse = couponRpcService.query(param);
        //如果名称重复，则返回错误信息
        if (baseResponse.getReturnCode().intValue() == ReturnCodeEnum.CODE_1000.getCode().intValue()) {
            returnResponse = BackResponseUtil.getBaseResponse(ReturnCodeEnum.CODE_1005.getCode());
            returnResponse.setMessage("名称不可重复");
        }

        return returnResponse;
    }
}