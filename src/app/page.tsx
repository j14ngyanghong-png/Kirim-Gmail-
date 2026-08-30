import { StoreProvider } from '@/lib/store'
import { AppRoot } from '@/components/AppRoot'

export default function Page() {
  return (
    <StoreProvider>
      <AppRoot />
    </StoreProvider>
  )
}
