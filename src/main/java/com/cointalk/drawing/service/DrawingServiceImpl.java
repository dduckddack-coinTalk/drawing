package com.cointalk.drawing.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cointalk.drawing.domain.DrawingData;
import com.cointalk.drawing.domain.DrawingResponse;
import com.cointalk.drawing.domain.DrawingResponseInnerData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDBException;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrawingServiceImpl implements DrawingService {

    private final AmazonS3Client amazonS3Client;
    private final InfluxDBTemplate<Point> influxDBTemplate;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;  // S3 버킷 이름

    @Value("${cloud.aws.localStoragePath}")
    public String localStoragePath;  // 로컬 저장소 경로

    @Override
    public DrawingResponse uploadImage(MultiValueMap<String, Part> data) {
        DrawingResponse result = new DrawingResponse();
        Map<String, Object> drawingDataMap = new HashMap<>();
        try {

            data.forEach((arg1, arg2) -> {
                if (!arg1.equals("image")) {  // 이미지 파일이 아닌 파라미터 저장
                    arg2.forEach(body -> {
                        body.content().subscribe(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            String valueFromMultipart = new String(bytes, StandardCharsets.UTF_8);
                            drawingDataMap.put(arg1, valueFromMultipart);
                        });
                    });
                } else {  // formData 로 받은 파일파트 이미지 파일 업로드 및 S3 업로드 주소 저장
                    Mono.just(data.get("image"))
                            .flatMapMany(Flux::fromIterable)
                            .cast(FilePart.class)
                            .map(filePart -> {
                                String filename = String.valueOf(UUID.randomUUID()) + filePart.filename();
                                // 파일파트 받아서 로컬저장소에 저장 (비동기 논블럭킹으로 실행) -> 로컬 저장소에 저장된 파일로 s3업로드 -> s3파일 업로드 후 로컬 저장소 파일 삭제
                                filePart.transferTo(Paths.get(localStoragePath + filename)).subscribe();
                                drawingDataMap.put("filename", filename);
                                return filePart;
                            })
                            .subscribe();
                }
            });

            String filename = (String) drawingDataMap.get("filename");
            // 로컬 저장소에 저장된 파일로 s3업로드 -> s3파일 업로드 후 로컬 저장소 파일 삭제
            File files = checkCreateFile(filename);

            amazonS3Client.putObject(new PutObjectRequest(bucket, filename, files).withCannedAcl(CannedAccessControlList.PublicRead));
            String amazonS3Url = amazonS3Client.getUrl(bucket, filename).toString();

            for(int i=0; i<3; i++){
                // s3업로드 완료 확인시 로컬 저장소 파일 삭제. 업로드 실패하면 3번 재시도
                if(deleteLocalFile(filename, files)){
                    break;
                }
            }

            // influxDB에 유저, 코인종류, 차트시간, 이미지 주소 저장
            Point point = Point.measurement("drawing_image")
                    .time(Long.parseLong((String) drawingDataMap.get("time")), TimeUnit.MILLISECONDS)
                    .tag("userId", (String) drawingDataMap.get("userId"))
                    .tag("image", amazonS3Url)
                    .addField("coin", (String) drawingDataMap.get("coin"))
                    .build();
            influxDBTemplate.write(point);

            result.setStatus("ok");
            result.setMessage(drawingDataMap);

        }catch (InfluxDBException e){
            result.setStatus("error");
            result.setMessage("influxDB 에러");
            e.printStackTrace();
        }catch (Exception e){
            result.setStatus("error");
            result.setMessage("기타에러");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Mono<DrawingResponse> getDrawingData(String userId) {

        DrawingResponse result = new DrawingResponse();
        List<DrawingResponseInnerData> resultList = new ArrayList<>();
        String q = "SELECT * FROM drawing_image " +
                "WHERE userId= '"+userId+"' " +
                "ORDER BY DESC";

        Query query = BoundParameterQuery.QueryBuilder.newQuery(q)
                .forDatabase("coin")
                .create();

        QueryResult queryResult = influxDBTemplate.query(query);

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

        return Mono.just(resultMapper.toPOJO(queryResult, DrawingData.class))
                .map(data-> {
                    data.forEach(item-> {
                        DrawingResponseInnerData drawingResponseInnerData =
                                new DrawingResponseInnerData(item.getTime().toEpochMilli(), item.getUserId(), item.getCoin(), item.getImage());
                        resultList.add(drawingResponseInnerData);
                    });
                    result.setStatus("ok");
                    result.setMessage(resultList);
                    return result;
                });
    }
    private File checkCreateFile(String filename) throws InterruptedException {
        File files = new File(localStoragePath + filename);
        boolean fileExist = files.exists();

        // 파일생성 체크 후 진행
        for(int i =0; i<3; i++){
            if(fileExist){
                break;
            }
            Thread.sleep(300);
            fileExist = files.exists();
        }

        return files;
    }

    private boolean deleteLocalFile(String filename, File files) throws InterruptedException {

        if (amazonS3Client.doesObjectExist(bucket, filename)) { // s3업로드 완료 확인시 로컬 저장소 파일 삭제
            // 파일용량이 너무 작은 파일은 간헐적으로 0kb로 올라가는 경우가 있는데 이에대한 예외처리
            for(int i=0; i<3; i++){
                long filesize = amazonS3Client.getObjectMetadata(bucket, filename).getContentLength();
                if(filesize <= 0){
                    amazonS3Client.putObject(new PutObjectRequest(bucket, filename, files).withCannedAcl(CannedAccessControlList.PublicRead));
                    Thread.sleep(300);
                }else {
                    log.info("s3 Upload done. fileSize : "+ filesize);
                    break;
                }
            }
            if (files.delete()) {
                log.info("local file delete");
            }
        }else {
            return false;
        }
        return true;
    }


}
