package cn.vpclub.coupon.admin.rpc;

import cn.vpclub.coupon.api.entity.OrderPaidEventLog;
import cn.vpclub.coupon.api.model.request.OrderPaidEventLogPageParam;
import cn.vpclub.coupon.query.api.OrderPaidEventLogProto;
import cn.vpclub.coupon.query.api.OrderPaidEventLogProto.OrderPaidEventLogDTO;
import cn.vpclub.coupon.query.api.OrderPaidEventLogProto.OrderPaidEventLogPageResponse;
import cn.vpclub.coupon.query.api.OrderPaidEventLogProto.OrderPaidEventLogResponse;
import cn.vpclub.coupon.query.api.OrderPaidEventLogServiceGrpc.OrderPaidEventLogServiceBlockingStub;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.fromGRpcMessage;
import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.toGRpcMessage;

/**
 * <p>
 * 订单支付成功事件日志表 rpc层数据传输
 * </p>
 *
 * @author yangqiao
 * @since 2017-12-22
 */
@Service
@Slf4j
@AllArgsConstructor
public class OrderPaidEventLogRpcService {

    private OrderPaidEventLogServiceBlockingStub blockingStub;

    public BaseResponse add(OrderPaidEventLog request) {
        OrderPaidEventLogDTO dto = (OrderPaidEventLogDTO) toGRpcMessage(request, OrderPaidEventLogDTO.newBuilder());
        OrderPaidEventLogResponse response = blockingStub.add(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, OrderPaidEventLog.class);
    }

    public BaseResponse update(OrderPaidEventLog request) {
        OrderPaidEventLogDTO dto = (OrderPaidEventLogDTO) toGRpcMessage(request, OrderPaidEventLogDTO.newBuilder());
        OrderPaidEventLogResponse response = blockingStub.update(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, OrderPaidEventLog.class);
    }

    public BaseResponse query(OrderPaidEventLog request) {
        OrderPaidEventLogDTO dto = (OrderPaidEventLogDTO) toGRpcMessage(request, OrderPaidEventLogDTO.newBuilder());
        OrderPaidEventLogResponse response = blockingStub.query(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, OrderPaidEventLog.class);
    }

    public BaseResponse delete(OrderPaidEventLog request) {
        OrderPaidEventLogDTO dto = (OrderPaidEventLogDTO) toGRpcMessage(request, OrderPaidEventLogDTO.newBuilder());
        OrderPaidEventLogResponse response = blockingStub.delete(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, OrderPaidEventLog.class);
    }

    public PageResponse page(OrderPaidEventLogPageParam request) {
        OrderPaidEventLogProto.OrderPaidEventLogRequest dto = (OrderPaidEventLogProto.OrderPaidEventLogRequest) toGRpcMessage
                (request, OrderPaidEventLogProto.OrderPaidEventLogRequest.newBuilder());
        OrderPaidEventLogPageResponse listResponse = blockingStub.page(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, OrderPaidEventLog.class);
    }
}
