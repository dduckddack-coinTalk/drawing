package com.cointalk.drawing.domain;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DrawingResponse {

    private String status;

    private Object message;

}
