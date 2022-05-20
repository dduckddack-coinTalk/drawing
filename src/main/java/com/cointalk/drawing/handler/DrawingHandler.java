package com.cointalk.drawing.handler;

import com.cointalk.drawing.domain.DrawingResponse;
import com.cointalk.drawing.domain.DrawingResponseInnerData;
import com.cointalk.drawing.service.DrawingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DrawingHandler {

    private final DrawingService drawingService;

    public Mono<ServerResponse> uploadImage(ServerRequest request){
        Mono<DrawingResponse> drawingResponseMono = request.multipartData().map(drawingService::uploadImage);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromProducer(drawingResponseMono, DrawingResponse.class));
    }

    public Mono<ServerResponse> getImage(ServerRequest request) {
        Mono<DrawingResponse> drawingResponseMono = request.bodyToMono(DrawingResponseInnerData.class)
                .flatMap(drawingService::getDrawingData)
                .log()
                ;

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(drawingResponseMono, DrawingResponse.class)
                .onErrorResume(error -> ServerResponse.badRequest().build())
                ;
    }

}
