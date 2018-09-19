package cn.vpclub.coupon.admin.rpc;

import cn.vpclub.coupon.api.entity.ThirdPartyCouponLog;
import cn.vpclub.coupon.api.model.request.ThirdPartyCouponLogPageParam;
import cn.vpclub.coupon.query.api.ThirdPartyCouponLogProto;
import cn.vpclub.coupon.query.api.ThirdPartyCouponLogProto.ThirdPartyCouponLogDTO;
import cn.vpclub.coupon.query.api.ThirdPartyCouponLogProto.ThirdPartyCouponLogPageResponse;
import cn.vpclub.coupon.query.api.ThirdPartyCouponLogProto.ThirdPartyCouponLogResponse;
import cn.vpclub.coupon.query.api.ThirdPartyCouponLogServiceGrpc.ThirdPartyCouponLogServiceBlockingStub;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.fromGRpcMessage;
import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.toGRpcMessage;

/**
 * <p>
 * 第三方发券服务调用日志表 rpc层数据传输
 * </p>
 *
 * @author yangqiao
 * @since 2017-12-22
 */
@Service
@Slf4j
@AllArgsConstructor
public class ThirdPartyCouponLogRpcService {

    private ThirdPartyCouponLogServiceBlockingStub blockingStub;

    public BaseResponse add(ThirdPartyCouponLog request) {
        ThirdPartyCouponLogDTO dto = (ThirdPartyCouponLogDTO) toGRpcMessage(request, ThirdPartyCouponLogDTO.newBuilder());
        ThirdPartyCouponLogResponse response = blockingStub.add(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ThirdPartyCouponLog.class);
    }

    public BaseResponse update(ThirdPartyCouponLog request) {
        ThirdPartyCouponLogDTO dto = (ThirdPartyCouponLogDTO) toGRpcMessage(request, ThirdPartyCouponLogDTO.newBuilder());
        ThirdPartyCouponLogResponse response = blockingStub.update(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ThirdPartyCouponLog.class);
    }

    public BaseResponse query(ThirdPartyCouponLog request) {
        ThirdPartyCouponLogDTO dto = (ThirdPartyCouponLogDTO) toGRpcMessage(request, ThirdPartyCouponLogDTO.newBuilder());
        ThirdPartyCouponLogResponse response = blockingStub.query(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ThirdPartyCouponLog.class);
    }

    public BaseResponse delete(ThirdPartyCouponLog request) {
        ThirdPartyCouponLogDTO dto = (ThirdPartyCouponLogDTO) toGRpcMessage(request, ThirdPartyCouponLogDTO.newBuilder());
        ThirdPartyCouponLogResponse response = blockingStub.delete(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ThirdPartyCouponLog.class);
    }

    public PageResponse page(ThirdPartyCouponLogPageParam request) {
        ThirdPartyCouponLogProto.ThirdPartyCouponLogRequest dto = (ThirdPartyCouponLogProto.ThirdPartyCouponLogRequest)
                toGRpcMessage(request, ThirdPartyCouponLogProto.ThirdPartyCouponLogRequest.newBuilder());
        ThirdPartyCouponLogPageResponse listResponse = blockingStub.page(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, ThirdPartyCouponLog.class);
    }
}
