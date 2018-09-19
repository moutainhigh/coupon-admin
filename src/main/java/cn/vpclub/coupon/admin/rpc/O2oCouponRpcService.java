package cn.vpclub.coupon.admin.rpc;

import cn.vpclub.coupon.api.entity.O2oCoupon;
import cn.vpclub.coupon.api.model.request.O2oCouponPageParam;
import cn.vpclub.coupon.api.requests.o2ocoupon.O2OCouponMixRequest;
import cn.vpclub.coupon.query.api.O2oCouponProto;
import cn.vpclub.coupon.query.api.O2oCouponProto.O2oCouponDTO;
import cn.vpclub.coupon.query.api.O2oCouponProto.O2oCouponListResponse;
import cn.vpclub.coupon.query.api.O2oCouponProto.O2oCouponPageResponse;
import cn.vpclub.coupon.query.api.O2oCouponProto.O2oCouponResponse;
import cn.vpclub.coupon.query.api.O2oCouponServiceGrpc;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.fromGRpcMessage;
import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.toGRpcMessage;

/**
 * <p>
 * o2o券码 rpc层数据传输
 * </p>
 *
 * @author yangqiao
 * @since 2017-12-22
 */
@Service
@Slf4j
@AllArgsConstructor
public class O2oCouponRpcService {

    private O2oCouponServiceGrpc.O2oCouponServiceBlockingStub blockingStub;

    public BaseResponse add(O2oCoupon request) {
        O2oCouponDTO dto = (O2oCouponDTO) toGRpcMessage(request, O2oCouponDTO.newBuilder());
        O2oCouponResponse response = blockingStub.add(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, O2oCoupon.class);
    }

    public BaseResponse update(O2oCoupon request) {
        O2oCouponDTO dto = (O2oCouponDTO) toGRpcMessage(request, O2oCouponDTO.newBuilder());
        O2oCouponResponse response = blockingStub.update(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, O2oCoupon.class);
    }

    public BaseResponse query(O2oCoupon request) {
        O2oCouponDTO dto = (O2oCouponDTO) toGRpcMessage(request, O2oCouponDTO.newBuilder());
        O2oCouponResponse response = blockingStub.query(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, O2oCoupon.class);
    }

    public BaseResponse findList(O2oCoupon request) {
        O2oCouponDTO dto = (O2oCouponDTO) toGRpcMessage(request, O2oCouponDTO.newBuilder());
        O2oCouponListResponse response = blockingStub.findList(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, O2oCoupon.class);
    }

    public BaseResponse delete(O2oCoupon request) {
        O2oCouponDTO dto = (O2oCouponDTO) toGRpcMessage(request, O2oCouponDTO.newBuilder());
        O2oCouponResponse response = blockingStub.delete(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, O2oCoupon.class);
    }

    public PageResponse findO2OCouponConsumeStatus(O2OCouponMixRequest request) {
        O2oCouponProto.O2OCouponMixRequest dto = (O2oCouponProto.O2OCouponMixRequest) toGRpcMessage(request, O2oCouponProto
                .O2OCouponMixRequest
                .newBuilder
                        ());
        O2oCouponProto.O2OCouponMixRequestPageResponse listResponse = blockingStub.findO2OCouponConsumeStatus(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, O2OCouponMixRequest.class);
    }

    public PageResponse page(O2oCouponPageParam request) {
        O2oCouponProto.O2oCouponRequest dto = (O2oCouponProto.O2oCouponRequest) toGRpcMessage(request, O2oCouponProto
                .O2oCouponRequest.newBuilder());
        O2oCouponPageResponse listResponse = blockingStub.page(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, O2oCoupon.class);
    }
}
