const WORK_FAVORITES_KEY_PREFIX = 'favorite_work_ids_'

const normalizeIds = (ids) => {
  if (!Array.isArray(ids)) return []
  return [...new Set(ids.map((id) => Number(id)).filter((id) => Number.isFinite(id)))]
}

export function getWorkFavoritesKey(username) {
  return `${WORK_FAVORITES_KEY_PREFIX}${username || 'guest'}`
}

export function loadFavoriteWorkIds(username) {
  try {
    const raw = localStorage.getItem(getWorkFavoritesKey(username))
    const parsed = raw ? JSON.parse(raw) : []
    return normalizeIds(parsed)
  } catch {
    return []
  }
}

export function saveFavoriteWorkIds(username, ids) {
  const normalized = normalizeIds(ids)
  localStorage.setItem(getWorkFavoritesKey(username), JSON.stringify(normalized))
  return normalized
}

export function addFavoriteWork(username, workId) {
  const next = [Number(workId), ...loadFavoriteWorkIds(username)]
  return saveFavoriteWorkIds(username, next)
}

export function removeFavoriteWork(username, workId) {
  const current = loadFavoriteWorkIds(username)
  return saveFavoriteWorkIds(username, current.filter((id) => id !== Number(workId)))
}
