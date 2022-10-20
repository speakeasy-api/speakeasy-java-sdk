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
    private SpeakeasyCaptureWriter writer = new SpeakeasyCaptureWriter();
    private boolean requestComplete = false;
    private boolean responseComplete = false;

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (HttpRequest.class.isInstance(message)) {
            this.request = new SpeakeasyNettyRequest(context, (HttpRequest) message);
            this.request.register(writer);
        }

        if (HttpContent.class.isInstance(message)) {
            this.request.buffer(((HttpContent) message).content());
        }

        if (ByteBuf.class.isInstance(message)) {
            this.request.buffer((ByteBuf) message);
        }

        if (LastHttpContent.class.isInstance(message)) {
            requestComplete = true;

            if (responseComplete) {
                capture();
            }
        }

        context.fireChannelRead(message);
    }

    @Override
    public void write(final ChannelHandlerContext context, final Object message, final ChannelPromise promise) {
        if (HttpResponse.class.isInstance(message)) {
            this.response = new SpeakeasyNettyResponse(context, (HttpResponse) message);
            this.response.register(writer);
        }

        if (HttpContent.class.isInstance(message)) {
            this.response.buffer(((HttpContent) message).content());
        }

        if (ByteBuf.class.isInstance(message)) {
            this.response.buffer((ByteBuf) message);
        }

        if (LastHttpContent.class.isInstance(message)) {
            responseComplete = true;

            if (requestComplete) {
                capture();
            }
        }

        context.write(message, promise);
    }

    private void capture() {
        new SpeakeasyCapture().capture(request, response);
    }
}
