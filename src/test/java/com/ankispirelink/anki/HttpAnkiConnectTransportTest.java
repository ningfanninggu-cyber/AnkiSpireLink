package com.ankispirelink.anki;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpAnkiConnectTransportTest {

    @Test
    void blankEndpointUsesDefaultEndpoint() throws Exception {
        HttpAnkiConnectTransport transport = new HttpAnkiConnectTransport("  ", 3000);

        assertEquals(HttpAnkiConnectTransport.DEFAULT_ENDPOINT, stringField(transport, "endpoint"));
    }

    @Test
    void timeoutIsClampedToPositiveValue() throws Exception {
        HttpAnkiConnectTransport transport = new HttpAnkiConnectTransport("http://127.0.0.1:8765", 0);

        assertEquals(1, intField(transport, "timeoutMillis"));
    }

    private static String stringField(HttpAnkiConnectTransport transport, String name) throws Exception {
        return (String) field(name).get(transport);
    }

    private static int intField(HttpAnkiConnectTransport transport, String name) throws Exception {
        return field(name).getInt(transport);
    }

    private static Field field(String name) throws Exception {
        Field field = HttpAnkiConnectTransport.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
