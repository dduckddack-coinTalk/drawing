package com.cointalk.drawing.router;

import com.cointalk.drawing.handler.DrawingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration(proxyBeanMethods = false)
public class DrawingRouter {

    @Bean
    public RouterFunction<ServerResponse> route(DrawingHandler cointalkDrawingHandler) {
        return RouterFunctions
                .route(POST("/drawing/uploadImage"), cointalkDrawingHandler::uploadImage) // 차트 이미지 S3에 저장
                .andRoute(GET("/drawing/getImage"), cointalkDrawingHandler::getImage) // 사용자 id값으로 이미지 제공
                ;
    }
}
