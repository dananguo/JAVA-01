package io.github.kimmking.gateway.outbound.httpclient4;

import hw1.OkHttpUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author zhangyichi006
 * @date 2021/1/29 11:36
 * @description
 */
public class HttpOutboundHandler {

    private List<String> backendUrls;

    public HttpOutboundHandler(List<String> backendUrls) {
        List<String> newUrls = new ArrayList<>();
        for (String backendUrl : backendUrls) {
            backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
            newUrls.add(backendUrl);
        }
        this.backendUrls = newUrls;
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        this.backendUrls.forEach(backendUrl -> {
            String url = backendUrl + fullRequest.uri();
            FullHttpResponse response = null;
            try {
                //
                HttpHeaders headers = fullRequest.headers();
                //
                String body = OkHttpUtils.getAsString(url, headers);
                byte[] bytesArray = body.getBytes("UTF-8");
                response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(bytesArray));
                response.headers().set("Content-Type", "application/json");
                response.headers().set("X-proxy-tag", this.getClass().getSimpleName());
                response.headers().setInt("Content-Length", bytesArray.length);
            } catch (Throwable e) {
                e.printStackTrace();
                response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
                exceptionCaught(ctx, e);
            } finally {
                if (null == response) {
                    response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
                }
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    //response.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(response);
                }
                ctx.flush();
                //ctx.close();
            }
        });

    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
