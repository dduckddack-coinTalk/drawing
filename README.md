# Drawing-Image Server
사용자가 편집한 차트 이미지를 저장하고 불러올 수 있는 기능을 제공하는 서버


### HowTo Setting


#### 1. Amazon S3 등록 및 설정
1. 아마존 S3 등록 
2. 관련 설정 하단 aplication.yml 에 등록

#### 2. influxDB 설치 및 설정 등록
1. influxDB 설치
2. 데이터베이스, 로그인 계정 생성 후 아래 설정에 등록


#### aplication.yml 설정

```yml
server:
  port: 9092

cloud:
  aws:
    credentials:
      accessKey: 아마존 엑세스 키 입력
      secretKey: 아마존 시크릿 키 입력
    s3:
      bucket: 이미지 파일 업로드/조회할 S3 버켓명 입력
    region:
      static: 리전명 입력
    stack:
      auto: false
    localStoragePath: 로컬 파일 저장소 위치 입력

logging:
  level:
    root: info
    com:
      amazonaws:
        util:
          EC2MetadataUtils: error

spring:
  influxdb:
    url: influxDB 서버 입력
    database: 데이터 베이스명 입력
    username: 접속 아이디 입력
    retention-policy: autogen

```













# REST API

### URI : `POST` / drawing / uploadImage
사용자가 편집한 차트 이미지를 저장하는 API.

Parameters
| name | type | Description | Default |
|:---|:---:|:---:|:---:|
| `image` |File|업로드할 이미지파일||
| `userId` |String|해당 유저 아이디||
| `time` |String|차트 시간||
| `coin` |String|해당 차트의 코인명||

Responses
<pre>
{
    "status": "ok",
    "message": {
        "image": "https://bucket.s3.ap-northeast-2.amazonaws.com/94ccb1ac-f2eb-4b20-976b-6aa749b47a87highjpg.jpeg",
        "time": "1652687706",
        "userId": "filetest",
        "coin": "XRP"
    }
}
</pre>





<br></br>
### URI : `POST` / drawing / getImage
사용자가 저장한 특정 코인의 차트 이미지를 조회하고 불러오는 API.

Parameters
| name | type | Description | Default |
|:---|:---:|:---:|:---:|
| `userId` |String|유저 아이디||
| `coin` |String|코인명 ||

Responses
<pre>
{
    "status": "ok",
    "message": [
        {
            "time": 1652687706,
            "userId": "filetest",
            "image": "https://bucket.s3.ap-northeast-2.amazonaws.com/f421115b-faa2-48f8-b7b0-8437b37d0306highjpg.jpeg",
            "coin": "XRP"
        },
        {
            "time": 1652688805,
            "userId": "filetest",
            "image": "https://bucket.s3.ap-northeast-2.amazonaws.com/94ccb1ac-f2eb-4b20-976b-6aa749b47a87doge.jpeg",
            "coin": "XRP"
        }
    ]
}
</pre>




