import { useEffect, useMemo, useState } from 'react'
import { getNowPlayingMovies } from '../api/movie'
import type { Movie } from '../types/movie'
import MovieCard from '../components/MovieCard'

const VISIBLE_COUNT = 10

const HomePage = () => {
  const [movies, setMovies] = useState<Movie[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [showAll, setShowAll] = useState(false)

  useEffect(() => {
    let isMounted = true

    const fetchMovies = async () => {
      setIsLoading(true)
      try {
        const data = await getNowPlayingMovies()
        if (isMounted) setMovies(data)
      } finally {
        if (isMounted) setIsLoading(false)
      }
    }

    fetchMovies()
    return () => {
      isMounted = false
    }
  }, [])

  // 기본은 10개만 잘라서 보여주고, "더보기"를 누르면 전체를 노출한다.
  const visibleMovies = useMemo(
    () => (showAll ? movies : movies.slice(0, VISIBLE_COUNT)),
    [movies, showAll],
  )
  const hasMore = !showAll && movies.length > VISIBLE_COUNT

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <p className="font-mono text-xs tracking-marquee text-reel-gold mb-2">NOW SHOWING</p>
      <h1 className="font-display text-4xl tracking-marquee text-ink-light mb-8">
        영화 예매
      </h1>

      {isLoading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-x-4 gap-y-8">
          {Array.from({ length: 10 }).map((_, i) => (
            <div key={i} className="flex flex-col animate-pulse">
              <div className="aspect-[2/3] w-full rounded-xl bg-marquee-card border border-marquee-line" />
              <div className="mt-3 h-4 w-3/4 rounded bg-marquee-card" />
              <div className="mt-2 h-3 w-1/3 rounded bg-marquee-card" />
            </div>
          ))}
        </div>
      ) : movies.length === 0 ? (
        <p className="font-body text-ink-muted py-16 text-center">
          현재 상영 중인 영화가 없습니다.
        </p>
      ) : (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-x-4 gap-y-8">
            {visibleMovies.map((movie) => (
              <MovieCard key={movie.id} movie={movie} />
            ))}
          </div>

          {hasMore && (
            <div className="flex justify-center mt-10">
              <button
                type="button"
                onClick={() => setShowAll(true)}
                className="font-body text-sm px-6 py-2.5 rounded-lg border border-marquee-line
                  text-ink-light hover:border-reel-gold transition-colors"
              >
                더보기
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

export default HomePage
