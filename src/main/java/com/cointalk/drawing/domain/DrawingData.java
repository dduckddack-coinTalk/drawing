package com.cointalk.drawing.domain;

import lombok.*;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Measurement(name = "drawing_image")
public class DrawingData {

    @Column(name = "time")
    private Instant time;

    @Column(name = "userId")
    private String userId;

    @Column(name = "image")
    private String image;

    @Column(name = "coin")
    private String coin;

}
