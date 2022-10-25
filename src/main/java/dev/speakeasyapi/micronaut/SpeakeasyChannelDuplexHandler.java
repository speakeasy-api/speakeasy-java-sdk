package dev.speakeasyapi.micronaut;

import javax.annotation.concurrent.NotThreadSafe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

@NotThreadSafe
public class SpeakeasyChannelDuplexHandler extends ChannelDuplexHandler {
    private SpeakeasyNettyRequest request;
    private SpeakeasyNettyResponse response;
    private SpeakeasyCaptureWriter writer;

    private boolean requestCaptured = false;
    private boolean responseCaptured = false;
    private boolean captured = false;

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (HttpRequest.class.isInstance(message)) {
            this.captured = false;
            this.requestCaptured = false;
            this.responseCaptured = false;

            HttpRequest httpRequest = (HttpRequest) message;
            this.request = new SpeakeasyNettyRequest(httpRequest);
            this.writer = new SpeakeasyCaptureWriter();
            this.request.register(this.writer);
        }

        if (HttpContent.class.isInstance(message)) {
            this.request.buffer(((HttpContent) message).content());
        }

        if (ByteBuf.class.isInstance(message)) {
            this.request.buffer((ByteBuf) message);
        }

        if (LastHttpContent.class.isInstance(message)) {
            this.requestCaptured = true;
            if (!captured && this.responseCaptured) {
                capture();
            }
        }

        context.fireChannelRead(message);
    }

    @Override
    public void write(final ChannelHandlerContext context, final Object message, final ChannelPromise promise) {
        if (HttpResponse.class.isInstance(message)) {
            HttpResponse httpResponse = (HttpResponse) message;
            this.response = new SpeakeasyNettyResponse(httpResponse);
            this.response.register(this.writer);
        }

        if (HttpContent.class.isInstance(message)) {
            this.response.buffer(((HttpContent) message).content());
        }

        if (ByteBuf.class.isInstance(message)) {
            this.response.buffer((ByteBuf) message);
        }

        if (LastHttpContent.class.isInstance(message)) {
            this.responseCaptured = true;
            if (!captured && this.requestCaptured) {
                capture();
            }
        }

        context.write(message, promise);
    }

    private synchronized void capture() {
        captured = true;
        new SpeakeasyCapture().capture(request, response);
    }
}
