/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package ninja;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Handles chunks of data which contains signatures generated by the AWS SDK.
 * <p>
 * We currently do not check the signature of each token, as we checked the request signature.
 * This seems to do the job for a test / mock sever for now.
 */
class SignedChunkHandler extends sirius.web.http.InputStreamHandler {
    @Override
    public void handle(ByteBuf content, boolean last) throws IOException {
        if (!content.isReadable()) {
            super.handle(content, last);
            return;
        }
        String lengthString = readChunkLengthHex(content);
        int lengthOfData = Integer.parseInt(lengthString, 16);

        skipSignature(content);

        super.handle(content.slice(content.readerIndex(), lengthOfData), last);
    }

    private void skipSignature(ByteBuf content) {
        while (content.isReadable()) {
            if (content.readByte() == '\r' && content.readByte() == '\n') {
                return;
            }
        }
    }

    @NotNull
    private String readChunkLengthHex(ByteBuf content) {
        StringBuilder lengthString = new StringBuilder();
        while (content.isReadable()) {
            byte data = content.readByte();
            if (data == ';') {
                return lengthString.toString();
            }
            lengthString.append((char) data);
        }
        return lengthString.toString();
    }
}
