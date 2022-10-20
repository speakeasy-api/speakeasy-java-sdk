package dev.speakeasyapi.micronaut;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SpeakeasyCaptureWriter {
    final int MAX_BUFFER_SIZE = 1 * 1024 * 1024;

    private ByteBuf reqBuffer = Unpooled.buffer();
    private ByteBuf resBuffer = Unpooled.buffer();

    private boolean reqValid = true;
    private boolean resValid = true;

    public void writeRequest(final ByteBuf content) {
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
        return reqBuffer.array();
    }

    public byte[] getResBuffer() {
        return resBuffer.array();
    }

    public boolean isReqValid() {
        return reqValid;
    }

    public boolean isResValid() {
        return resValid;
    }
}
