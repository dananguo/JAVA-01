package io.github.kimmking.gateway.outbound.okhttp;

import hw1.OkHttpUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OkhttpOutboundHandler {

    private List<String> backendUrls;

    public OkhttpOutboundHandler(List<String> backendUrls) {
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
                HttpHeaders headers = fullRequest.headers();
                String body = OkHttpUtils.getAsString(url, headers);
                byte[] bytesArray = body.getBytes(StandardCharsets.UTF_8);
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytesArray));
                response.headers().set("Content-Type", "application/json").set("X-proxy-tag", this.getClass().getSimpleName())
                        .setInt("Content-Length", bytesArray.length);
            } catch (IOException e) {
                e.printStackTrace();
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
                exceptionCaught(ctx, e);
            } finally {
                if (null == response) {
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
                }
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
                ctx.flush();
            }
        });
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
