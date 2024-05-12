package org.example;

import io.netty.channel.ChannelHandlerContext;

public record ChatMessage(String senderName, String messageText, ChannelHandlerContext channel) {

    @Override
    public String toString() {
        return senderName + ": " + messageText;
    }
}


