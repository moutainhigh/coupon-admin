package cn.vpclub.coupon.admin.config;


import cn.vpclub.coupon.query.api.CouponDetailServiceGrpc;
import cn.vpclub.coupon.query.api.CouponServiceGrpc;
import cn.vpclub.coupon.query.api.O2oCouponServiceGrpc;
import cn.vpclub.coupon.query.api.OrderPaidEventLogServiceGrpc;
import cn.vpclub.coupon.query.api.ProductThirdpartyRelatedServiceGrpc;
import cn.vpclub.coupon.query.api.ThirdPartyCouponLogServiceGrpc;
import cn.vpclub.spring.boot.grpc.annotations.GRpcClient;
import io.grpc.ManagedChannel;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * o2o券码 rpc server连接配置
 * </p>
 *
 * @author yangqiao
 * @since 2017-12-22
 */
@Configuration
@EnableAutoConfiguration
public class GrpcConfiguration {
    @GRpcClient("coupon-query")
    private ManagedChannel channel;

    @Bean
    public O2oCouponServiceGrpc.O2oCouponServiceBlockingStub o2oCouponServiceBlockingStub() {
        return O2oCouponServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public CouponDetailServiceGrpc.CouponDetailServiceBlockingStub couponDetailServiceBlockingStub() {
        return CouponDetailServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public CouponServiceGrpc.CouponServiceBlockingStub couponServiceBlockingStub() {
        return CouponServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public OrderPaidEventLogServiceGrpc.OrderPaidEventLogServiceBlockingStub orderPaidEventLogServiceBlockingStub() {
        return OrderPaidEventLogServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public ProductThirdpartyRelatedServiceGrpc.ProductThirdpartyRelatedServiceBlockingStub
    productThirdpartyRelatedServiceBlockingStub() {
        return ProductThirdpartyRelatedServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public ThirdPartyCouponLogServiceGrpc.ThirdPartyCouponLogServiceBlockingStub thirdPartyCouponLogServiceBlockingStub() {
        return ThirdPartyCouponLogServiceGrpc.newBlockingStub(channel);
    }

}
