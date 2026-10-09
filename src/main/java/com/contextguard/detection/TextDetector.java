package com.contextguard.detection;

import java.util.List;

public interface TextDetector {

    List<Detection> detect(String jsonPath, String text);
}