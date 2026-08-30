const rupiah = new Intl.NumberFormat('id-ID', {
  style: 'currency',
  currency: 'IDR',
  maximumFractionDigits: 0,
})

export function formatRupiah(amount: number): string {
  try {
    return rupiah.format(amount)
  } catch {
    return `Rp ${amount}`
  }
}

const dateFmt = new Intl.DateTimeFormat('id-ID', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

export function formatDate(timestamp: number): string {
  try {
    return dateFmt.format(new Date(timestamp))
  } catch {
    return '-'
  }
}

export function formatRelative(timestamp: number): string {
  const diff = Date.now() - timestamp
  const min = Math.floor(diff / 60000)
  if (min < 1) return 'Baru saja'
  if (min < 60) return `${min} menit lalu`
  const hr = Math.floor(min / 60)
  if (hr < 24) return `${hr} jam lalu`
  const day = Math.floor(hr / 24)
  return `${day} hari lalu`
}

export function maskEmail(email: string): string {
  const parts = email.split('@')
  if (parts.length !== 2) return email
  const [name, domain] = parts
  const maskedName =
    name.length > 3 ? `${name.slice(0, 2)}***${name.slice(-1)}` : `${name}***`
  return `${maskedName}@${domain}`
}

export function maskPassword(password: string): string {
  if (password.length <= 2) return '••••••'
  return password.slice(0, 2) + '•'.repeat(Math.max(6, password.length) - 2)
}
