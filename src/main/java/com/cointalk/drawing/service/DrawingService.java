package com.cointalk.drawing.service;

import com.cointalk.drawing.domain.DrawingResponse;
import com.cointalk.drawing.domain.DrawingResponseInnerData;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

public interface DrawingService {

    // 차트 이미지 Amazon S3에 저장
    DrawingResponse uploadImage(MultiValueMap<String, Part> data);

    // 차트 이미지와 사용자 정보 조회
    Mono<DrawingResponse> getDrawingData(DrawingResponseInnerData drawingResponseInnerData);

}
