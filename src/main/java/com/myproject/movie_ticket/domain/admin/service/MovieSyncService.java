package com.myproject.movie_ticket.domain.admin.service;

import com.myproject.movie_ticket.domain.movie.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieSyncService {
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;

    @Value("${external.api.kofic.key}")
    private String koficKey;
    @Value("${external.api.tmdb.key}")
    private String tmdbKey;
    @Value("${external.api.tmdb.image-prefix}")
    private String tmdbImagePrefix;

    @Async
    @Transactional
    public void syncBoxOffice(String targetDate) {
        log.info("영화 데이터 동기화 시작...");

        String koficUrl = String.format("http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=%s&targetDt=%s",
                koficKey, targetDate);

        try {
            log.info("영진위 API 호출 URL: {}", koficUrl);

            JsonNode koficResponse = restTemplate.getForObject(koficUrl, JsonNode.class);
            JsonNode movieList = koficResponse.path("boxOfficeResult")
                    .path("dailyBoxOfficeList");

            if (movieList.isEmpty()) {
                log.warn("영진위 응답이 비어있습니다. 날짜({})를 어제로 설정했는지 확인하세요.", targetDate);
                return;
            }

            for (JsonNode movieNode : movieList) {
                //영진위 api에서 영화코드, 영화제목 가져옴
                String koficMovieCd = movieNode.path("movieCd").asString();
                String title = movieNode.path("movieNm").asString();
                String rating = fetchKoficRating(koficMovieCd);
                String releaseDate = movieNode.path("openDt").asString();
                String releaseYear = releaseDate.substring(0, 4);

                log.info("영진위 데이터 수신 완료 - 영화명: {}", title);

                //영화제목으로 TMDB api 호출
                TmdbMovieDetail tmdbDtail = fetchTmdbDetails(title, releaseYear);

                if (tmdbDtail != null) {
                    saveOrUpdateMovie(koficMovieCd, title, rating, tmdbDtail);
                }
            }
        } catch (Exception e) {
            log.error("영화 동기화 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("영화 동기화 실패");
        }
    }

    private String fetchKoficRating(String movieCd) {
        String detailUrl = String.format("http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json?key=%s&movieCd=%s", koficKey, movieCd);
        try {
            JsonNode response = restTemplate.getForObject(detailUrl, JsonNode.class);
            JsonNode audits = response.path("movieInfoResult")
                    .path("movieInfo").path("audits");

            if (audits.isArray() && audits.size() > 0) {
                String rating = audits.get(0).path("watchGradeNm").asString();
                return rating;
            }
        } catch (Exception e) {
            log.warn("영진위 관람등급조회 실패 (movieCd: {}), 기본값 세팅\", movieCd");
        }
        return "ALL";
    }

    //내부 DTO
    private record TmdbMovieDetail(Integer runtime, String posterUrl, String overview) {}

    private TmdbMovieDetail fetchTmdbDetails(String title, String year) {
        try {
            String searchUrl = String.format("https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s&language=ko-KR",
                    tmdbKey, title, year);
            if (!year.isEmpty()) {
                searchUrl += "&primary_release_year=" + year;
            }

            JsonNode searchResponse = restTemplate.getForObject(searchUrl, JsonNode.class);
            JsonNode results = searchResponse.path("results");

            if (results.isEmpty()) return null;

            JsonNode firstResult = results.get(0);
            String tmdbId = firstResult.path("id").asString();
            String overview = firstResult.path("overview").asString();
            String posterPath = firstResult.path("poster_path").asString();
            String posterUrl = posterPath.equals("null") ? null : tmdbImagePrefix + posterPath;

            //TMDB 상세 API 상영시간 조회
            String detailUrl = String.format("https://api.themoviedb.org/3/movie/%s?api_key=%s&language=ko-KR", tmdbId, tmdbKey);
            JsonNode detailResponse = restTemplate.getForObject(detailUrl, JsonNode.class);
            int runtime = detailResponse.path("runtime").asInt(120);

            return new TmdbMovieDetail(runtime, posterUrl, overview);
        } catch (Exception e) {
            log.warn("TMDB 상세 조회 실패 - 영화:{}", title);
            log.warn("TMDB 상세 조회 실패 - 영화: {}, 원인: {}", title, e.getMessage());
            return null;
        }
    }
    private void saveOrUpdateMovie(String koficMovieCd, String title, String rating, TmdbMovieDetail detail) {
        movieRepository.findByKoficMovieCd(koficMovieCd).ifPresentOrElse(
                existingMovie -> existingMovie.updateMovieData(detail.runtime(), rating, detail.posterUrl(), detail.overview()),
                () -> {
                    Movie newMovie = Movie.builder()
                            .koficMovieCd(koficMovieCd)
                            .title(title)
                            .duration(detail.runtime)
                            .rating(rating)
                            .posterUrl(detail.posterUrl)
                            .description(detail.overview)
                            .build();
                    movieRepository.save(newMovie);
                }
        );
    }
}

