package com.cointalk.drawing.service;

import com.cointalk.drawing.domain.DrawingResponse;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

public interface DrawingService {

    DrawingResponse uploadImage(MultiValueMap<String, Part> data);

    Mono<DrawingResponse> getDrawingData(String userId);

}
