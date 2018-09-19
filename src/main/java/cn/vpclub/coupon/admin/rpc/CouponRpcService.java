package cn.vpclub.coupon.admin.rpc;

import cn.vpclub.coupon.api.entity.Coupon;
import cn.vpclub.coupon.api.model.request.CouponPageParam;
import cn.vpclub.coupon.api.requests.coupon.CouponLog;
import cn.vpclub.coupon.api.requests.coupon.CouponMixRequest;
import cn.vpclub.coupon.query.api.CouponProto;
import cn.vpclub.coupon.query.api.CouponProto.CouponDTO;
import cn.vpclub.coupon.query.api.CouponProto.CouponPageResponse;
import cn.vpclub.coupon.query.api.CouponProto.CouponResponse;
import cn.vpclub.coupon.query.api.CouponServiceGrpc.CouponServiceBlockingStub;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.fromGRpcMessage;
import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.toGRpcMessage;

/**
 * <p>
 * 卡券类型表 rpc层数据传输
 * </p>
 *
 * @author yangqiao
 * @since 2017-12-22
 */
@Service
@Slf4j
@AllArgsConstructor
public class CouponRpcService {

    private CouponServiceBlockingStub blockingStub;

    public BaseResponse add(Coupon request) {
        CouponDTO dto = (CouponDTO) toGRpcMessage(request, CouponDTO.newBuilder());
        CouponResponse response = blockingStub.add(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, Coupon.class);
    }

    public BaseResponse update(Coupon request) {
        CouponDTO dto = (CouponDTO) toGRpcMessage(request, CouponDTO.newBuilder());
        CouponResponse response = blockingStub.update(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, Coupon.class);
    }

    public BaseResponse loseBatchEfficacy(Coupon request) {
        CouponDTO dto = (CouponDTO) toGRpcMessage(request, CouponDTO.newBuilder());
        CouponResponse response = blockingStub.loseBatchEfficacy(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, Coupon.class);
    }

    public BaseResponse query(Coupon request) {
        CouponDTO dto = (CouponDTO) toGRpcMessage(request, CouponDTO.newBuilder());
        CouponResponse response = blockingStub.query(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, Coupon.class);
    }

    public BaseResponse delete(Coupon request) {
        CouponDTO dto = (CouponDTO) toGRpcMessage(request, CouponDTO.newBuilder());
        CouponResponse response = blockingStub.delete(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, Coupon.class);
    }

    public PageResponse findCouponLog(CouponLog request) {
        CouponProto.CouponLog dto = (CouponProto.CouponLog) toGRpcMessage(request, CouponProto.CouponLog.newBuilder
                ());
        CouponProto.CouponLogPageResponse listResponse = blockingStub.findCouponLog(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, CouponLog.class);
    }

    public PageResponse findCouponBatch(CouponMixRequest request) {
        CouponProto.CouponMixRequest dto = (CouponProto.CouponMixRequest) toGRpcMessage(request, CouponProto.CouponMixRequest
                .newBuilder
                        ());

        CouponProto.CouponMixRequestPageResponse listResponse = blockingStub.findCouponBatch(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, CouponMixRequest.class);
    }

    public PageResponse findCouponBatchOffDateExpires(CouponMixRequest request) {
        CouponProto.CouponMixRequest dto = (CouponProto.CouponMixRequest) toGRpcMessage(request, CouponProto.CouponMixRequest
                .newBuilder
                        ());
        CouponProto.CouponMixRequestPageResponse listResponse = blockingStub.findCouponBatchOffDateExpires(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, CouponMixRequest.class);
    }

    public BaseResponse reSendMsg(CouponMixRequest request) {
        CouponProto.CouponMixRequest dto = (CouponProto.CouponMixRequest) toGRpcMessage(request, CouponProto.CouponMixRequest
                .newBuilder
                        ());
        CouponProto.CouponResponse listResponse = blockingStub.reSendMsg(dto);
        return (BaseResponse) fromGRpcMessage(listResponse, BaseResponse.class, CouponResponse.class);
    }

    public PageResponse page(CouponPageParam request) {
        CouponProto.CouponRequest dto = (CouponProto.CouponRequest) toGRpcMessage(request, CouponProto.CouponRequest.newBuilder
                ());
        CouponPageResponse listResponse = blockingStub.page(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, Coupon.class);
    }
}
