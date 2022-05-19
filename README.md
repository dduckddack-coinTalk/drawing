# Drawing-Image Server
사용자가 편집한 차트 이미지를 저장하고 불러올 수 있는 기능을 제공하는 서버

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
### URI : `GET` / drawing / getImage
사용자가 저장한 차트 이미지를 조회하고 불러오는 API.

Parameters
| name | type | Description | Default |
|:---|:---:|:---:|:---:|
| `userId` |String|조회할 유저의 아이디||

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




