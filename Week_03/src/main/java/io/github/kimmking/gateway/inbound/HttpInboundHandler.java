package io.github.kimmking.gateway.inbound;

import hw2.ProxyBizFilter;
import io.github.kimmking.gateway.filter.HeaderHttpRequestFilter;
import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.github.kimmking.gateway.outbound.httpclient4.HttpOutboundHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private final List<String> proxyServer;
    private HttpOutboundHandler handler;
    private HttpRequestFilter filter = new HeaderHttpRequestFilter();

    public HttpInboundHandler(List<String> proxyServer) {
        this.proxyServer = proxyServer;
        this.handler = new HttpOutboundHandler(this.proxyServer);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("==== channelReadComplete(ChannelHandlerContext ctx)");
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("==== channelRead(ChannelHandlerContext ctx, Object msg) ");

        if (!(msg instanceof FullHttpRequest)) {
            return;
        }
        FullHttpRequest fullRequest = (FullHttpRequest) msg;
        try {
            filter.filter(fullRequest, ctx);
            ProxyBizFilter.newInstance().filter(fullRequest, ctx);
            handler.handle(fullRequest, ctx);


        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(e.getMessage());
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);

            if (!HttpUtil.isKeepAlive(fullRequest)) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                //response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
            ctx.flush();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    //    private void handlerTest(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
    //        FullHttpResponse response = null;
    //        try {
    //            String value = "hello,kimmking";
    //            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(value.getBytes("UTF-8")));
    //            response.headers().set("Content-Type", "application/json");
    //            response.headers().setInt("Content-Length", response.content().readableBytes());
    //
    //        } catch (Exception e) {
    //            logger.error("处理测试接口出错", e);
    //            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
    //        } finally {
    //            if (fullRequest != null) {
    //                if (!HttpUtil.isKeepAlive(fullRequest)) {
    //                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    //                } else {
    //                    response.headers().set(CONNECTION, KEEP_ALIVE);
    //                    ctx.write(response);
    //                }
    //            }
    //        }
    //    }
    //
    //    @Override
    //    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    //        cause.printStackTrace();
    //        ctx.close();
    //    }

}
