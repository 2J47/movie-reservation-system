import { Link } from 'react-router-dom'
import type { Movie } from '../types/movie'
import { getRatingTextClass } from '../utils/rating'

const POSTER_FALLBACK =
  'data:image/svg+xml;charset=UTF-8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="300" height="450">
      <rect width="100%" height="100%" fill="#1A1A1A"/>
      <text x="50%" y="50%" fill="#9A9A9A" font-family="sans-serif" font-size="16"
        text-anchor="middle" dominant-baseline="middle">NO IMAGE</text>
    </svg>`,
  )

interface MovieCardProps {
  movie: Movie
}

const MovieCard = ({ movie }: MovieCardProps) => {
  return (
    <Link
      to={`/movies/${movie.id}`}
      className="group flex flex-col"
    >
      <div className="relative aspect-[2/3] w-full overflow-hidden rounded-xl bg-marquee-card border border-marquee-line">
        <img
          src={movie.posterUrl}
          alt={`${movie.title} 포스터`}
          loading="lazy"
          onError={(e) => {
            e.currentTarget.onerror = null
            e.currentTarget.src = POSTER_FALLBACK
          }}
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
        />
        <span className={`absolute top-2 left-2 rounded-md bg-marquee-bg/85 border border-marquee-line px-2 py-0.5 font-mono text-[11px] ${getRatingTextClass(movie.rating)}`}>
          {movie.rating}
        </span>
      </div>

      <h3 className="mt-3 font-body text-sm font-semibold text-ink-light line-clamp-1 group-hover:text-reel-gold transition-colors">
        {movie.title}
      </h3>
      <p className="mt-0.5 font-mono text-xs text-ink-muted">{movie.duration}분</p>
    </Link>
  )
}

export default MovieCard
