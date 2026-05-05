package com.ankispirelink.anki;

import java.io.IOException;

public interface AnkiConnectTransport {
    String post(String requestJson) throws IOException;
}
