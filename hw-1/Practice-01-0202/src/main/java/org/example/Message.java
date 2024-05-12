package org.example;

import java.io.Serializable;

public record Message(int length, byte[] data) implements Serializable {
}
