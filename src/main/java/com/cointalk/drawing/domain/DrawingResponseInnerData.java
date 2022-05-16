package com.cointalk.drawing.domain;

import lombok.*;
import org.influxdb.annotation.Column;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DrawingResponseInnerData {

    private Object time;    // influxdb 시간값을 프론트에서 사용할수 있는 timestamp로 변환

    private String userId;

    private String image;

    private String coin;
}
