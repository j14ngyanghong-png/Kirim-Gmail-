import type { Metadata, Viewport } from 'next'
import { Plus_Jakarta_Sans } from 'next/font/google'
import './globals.css'

const jakarta = Plus_Jakarta_Sans({
  subsets: ['latin'],
  variable: '--font-jakarta',
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'Setor Gmail Rewards — Setor Akun, Dapatkan Cuan',
  description:
    'Platform reward untuk menyetorkan akun Gmail terverifikasi dan menarik saldo instan ke DANA, OVO, GoPay, dan bank. Rate tinggi, verifikasi kilat.',
  applicationName: 'Setor Gmail Rewards',
  keywords: ['setor gmail', 'reward', 'cuan', 'dana', 'ovo', 'gopay', 'e-wallet'],
}

export const viewport: Viewport = {
  themeColor: '#1e50ff',
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="id" className={`bg-background ${jakarta.variable}`}>
      <body className="font-sans antialiased">{children}</body>
    </html>
  )
}
