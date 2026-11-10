package com.blockradio.paper;

import java.nio.charset.StandardCharsets;

/**
 * Protocole du canal blockradio:main.
 *
 * Les chaînes sont encodées comme dans FriendlyByteBuf.writeUtf : taille en VarInt, puis octets UTF-8.
 *
 * Mod vers serveur :
 *   0x01 HELLO : VarInt version du protocole, String version du mod
 *   0x02 STATE : octet lecture (0 ou 1), VarInt volume (0 à 100), String titre
 * Serveur vers mod :
 *   0x10 COMMAND : octet action (voir Action)
 */
public final class Protocol {

    public static final String CHANNEL = "blockradio:main";
    public static final int VERSION = 1;

    public static final int C2S_HELLO = 0x01;
    public static final int C2S_STATE = 0x02;
    public static final int S2C_COMMAND = 0x10;

    /** Taille maximale acceptée pour une chaîne reçue, en octets. */
    private static final int MAX_STRING_BYTES = 4096;

    private Protocol() {
    }

    public enum Action {
        PLAY(1),
        STOP(2),
        VOLUME_UP(3),
        VOLUME_DOWN(4);

        public final int id;

        Action(int id) {
            this.id = id;
        }
    }

    public static byte[] command(Action action) {
        return new byte[] {(byte) S2C_COMMAND, (byte) action.id};
    }

    /** Retire les caractères de contrôle, coupe à max caractères et rogne les espaces. */
    public static String sanitize(String raw, int max) {
        StringBuilder sb = new StringBuilder();
        raw.codePoints()
                .filter(cp -> !Character.isISOControl(cp))
                .limit(max)
                .forEach(sb::appendCodePoint);
        return sb.toString().trim();
    }

    public static final class MalformedMessageException extends RuntimeException {
        public MalformedMessageException(String message) {
            super(message);
        }
    }

    public static final class Reader {
        private final byte[] data;
        private int pos;

        public Reader(byte[] data) {
            this.data = data;
        }

        public int readByte() {
            if (pos >= data.length) {
                throw new MalformedMessageException("fin de message inattendue");
            }
            return data[pos++] & 0xFF;
        }

        public int readVarInt() {
            int value = 0;
            int shift = 0;
            while (true) {
                int b = readByte();
                value |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) {
                    return value;
                }
                shift += 7;
                if (shift >= 35) {
                    throw new MalformedMessageException("VarInt trop long");
                }
            }
        }

        public String readString() {
            int length = readVarInt();
            if (length < 0 || length > MAX_STRING_BYTES) {
                throw new MalformedMessageException("chaîne trop longue : " + length);
            }
            if (pos + length > data.length) {
                throw new MalformedMessageException("chaîne tronquée");
            }
            String s = new String(data, pos, length, StandardCharsets.UTF_8);
            pos += length;
            return s;
        }
    }
}
