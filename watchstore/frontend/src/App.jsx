import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { queryClient } from './config/queryClient'
import { useAuthInit } from './hooks/useAuthInit'
import { router } from './routes/router'

function App() {
  useAuthInit()

  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  )
}

export default App
