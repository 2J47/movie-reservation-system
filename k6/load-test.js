import http from 'k6/http';
import { check, sleep } from 'k6';

//부하 옵션 설정 (목표 트래픽)
export const options = {
    vus: 200,           // Virtual Users (가상 유저 수): 동시에 접속할 유저 수
    duration: '30s',   // 테스트 진행 시간
};

//가상 유저가 반복해서 실행할 행동 정의
export default function () {
    // 스프링부트 로컬 서버의 '현재 상영작 조회' API 엔드포인트
    const url = 'http://localhost:8080/api/v1/movies/now-playing';

    // GET 요청 전송
    const res = http.get(url);

    //검증 (응답 코드가 200 OK인지 확인)
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    //대기 시간 (실제 유저가 화면을 보고 다시 클릭하는 딜레이 1초 모방)
//    sleep(1);
}