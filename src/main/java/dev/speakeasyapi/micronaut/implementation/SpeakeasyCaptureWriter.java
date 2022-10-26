package dev.speakeasyapi.micronaut.implementation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SpeakeasyCaptureWriter {
    final int MAX_BUFFER_SIZE = 1 * 1024 * 1024;

    private ByteBuf reqBuffer = Unpooled.buffer();
    private ByteBuf resBuffer = Unpooled.buffer();

    private boolean reqValid = true;
    private boolean resValid = true;

    public void writeRequest(final ByteBuf content) {
        if (content.equals(Unpooled.EMPTY_BUFFER)) {
            return;
        }

        final int readIndex = content.readerIndex();

        final int readableBytes = content.readableBytes();

        if ((reqBuffer.readableBytes() + resBuffer.readableBytes() + readableBytes) > MAX_BUFFER_SIZE) {
            reqValid = false;
        } else if (reqValid) {
            reqBuffer.ensureWritable(readableBytes);
            content.readBytes(reqBuffer, readableBytes);
            content.readerIndex(readIndex);
        }
    }

    public void writeResponse(final ByteBuf content) {
        if (content.equals(Unpooled.EMPTY_BUFFER)) {
            return;
        }

        final int readIndex = content.readerIndex();

        final int readableBytes = content.readableBytes();

        if ((reqBuffer.readableBytes() + resBuffer.readableBytes() + readableBytes) > MAX_BUFFER_SIZE) {
            resValid = false;
        } else if (resValid) {
            resBuffer.ensureWritable(readableBytes);
            content.readBytes(resBuffer, readableBytes);
            content.readerIndex(readIndex);
        }
    }

    public byte[] getReqBuffer() {
        final int length = reqBuffer.readableBytes();

        if (length == reqBuffer.capacity()) {
            return reqBuffer.array();
        }

        final byte[] target = new byte[length];
        System.arraycopy(reqBuffer.array(), 0, target, 0, length);
        return target;
    }

    public byte[] getResBuffer() {
        final int length = resBuffer.readableBytes();

        if (length == resBuffer.capacity()) {
            return resBuffer.array();
        }

        final byte[] target = new byte[length];
        System.arraycopy(resBuffer.array(), 0, target, 0, length);
        return target;
    }

    public boolean isReqValid() {
        return reqValid;
    }

    public boolean isResValid() {
        return resValid;
    }
}
