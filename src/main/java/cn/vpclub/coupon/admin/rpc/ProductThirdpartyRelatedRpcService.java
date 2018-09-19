package cn.vpclub.coupon.admin.rpc;

import cn.vpclub.coupon.api.entity.ProductThirdpartyRelated;
import cn.vpclub.coupon.api.model.request.ProductThirdpartyRelatedPageParam;
import cn.vpclub.coupon.query.api.ProductThirdpartyRelatedProto;
import cn.vpclub.coupon.query.api.ProductThirdpartyRelatedProto.ProductThirdpartyRelatedDTO;
import cn.vpclub.coupon.query.api.ProductThirdpartyRelatedProto.ProductThirdpartyRelatedPageResponse;
import cn.vpclub.coupon.query.api.ProductThirdpartyRelatedProto.ProductThirdpartyRelatedResponse;
import cn.vpclub.coupon.query.api.ProductThirdpartyRelatedServiceGrpc.ProductThirdpartyRelatedServiceBlockingStub;
import cn.vpclub.moses.core.model.response.BaseResponse;
import cn.vpclub.moses.core.model.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.fromGRpcMessage;
import static cn.vpclub.moses.utils.grpc.GRpcMessageConverter.toGRpcMessage;

/**
 * <p>
 * 商品与第三方服务关联表 rpc层数据传输
 * </p>
 *
 * @author yangqiao
 * @since 2017-12-22
 */
@Service
@Slf4j
@AllArgsConstructor
public class ProductThirdpartyRelatedRpcService {

    private ProductThirdpartyRelatedServiceBlockingStub blockingStub;

    public BaseResponse add(ProductThirdpartyRelated request) {
        ProductThirdpartyRelatedDTO dto = (ProductThirdpartyRelatedDTO) toGRpcMessage(request, ProductThirdpartyRelatedDTO
                .newBuilder());
        ProductThirdpartyRelatedResponse response = blockingStub.add(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ProductThirdpartyRelated.class);
    }

    public BaseResponse update(ProductThirdpartyRelated request) {
        ProductThirdpartyRelatedDTO dto = (ProductThirdpartyRelatedDTO) toGRpcMessage(request, ProductThirdpartyRelatedDTO
                .newBuilder());
        ProductThirdpartyRelatedResponse response = blockingStub.update(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ProductThirdpartyRelated.class);
    }

    public BaseResponse query(ProductThirdpartyRelated request) {
        ProductThirdpartyRelatedDTO dto = (ProductThirdpartyRelatedDTO) toGRpcMessage(request, ProductThirdpartyRelatedDTO
                .newBuilder());
        ProductThirdpartyRelatedResponse response = blockingStub.query(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ProductThirdpartyRelated.class);
    }

    public BaseResponse delete(ProductThirdpartyRelated request) {
        ProductThirdpartyRelatedDTO dto = (ProductThirdpartyRelatedDTO) toGRpcMessage(request, ProductThirdpartyRelatedDTO
                .newBuilder());
        ProductThirdpartyRelatedResponse response = blockingStub.delete(dto);
        return (BaseResponse) fromGRpcMessage(response, BaseResponse.class, ProductThirdpartyRelated.class);
    }

    public PageResponse page(ProductThirdpartyRelatedPageParam request) {
        ProductThirdpartyRelatedProto.ProductThirdpartyRelatedRequest dto = (ProductThirdpartyRelatedProto
                .ProductThirdpartyRelatedRequest) toGRpcMessage(request, ProductThirdpartyRelatedProto
                .ProductThirdpartyRelatedRequest.newBuilder());
        ProductThirdpartyRelatedPageResponse listResponse = blockingStub.page(dto);
        return (PageResponse) fromGRpcMessage(listResponse, PageResponse.class, ProductThirdpartyRelated.class);
    }
}
