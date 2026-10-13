import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data'

const tokens = new SharedArray('jwt tokens', function () {
    return JSON.parse(open('./tokens.json'));
});

export const options = {
    vus: 100,          // 가상 유저 100명
    duration: '10s',   // DB가 데드락에 빠지거나 뻗을 수 있으므로 10초로 짧게
};

export default function () {
    //API 엔드포인트 URL 및 파라미터
    const myToken = tokens[__VU - 1];
    const url = 'http://localhost:8080/api/v1/reservations';

    //예매할 좌석 정보 (JSON Body)
    const payload = JSON.stringify({
        scheduleId: 53,
        seatIds: [908, 909]
    });

    //HTTP 헤더 설정 (JSON 타입 명시, 인증 토큰 등)
    const params = {
        headers: {
            'Content-Type': 'application/json',
             'Authorization': `Bearer ${myToken}`
        },
    };

    //POST 요청 전송
    const res = http.post(url, payload, params);

    //검증 로직
    check(res, {
        '예매 성공 (201 Created)': (r) => r.status === 200 || r.status === 201,
        '이미 선점된 좌석 예외 (400 or 409)': (r) => r.status === 400 || r.status === 409,
        '서버/DB 터짐 (500 Error)': (r) => r.status === 500,
    });
}