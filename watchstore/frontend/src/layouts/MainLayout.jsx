import { Outlet, ScrollRestoration } from 'react-router'
import Footer from '../components/common/Footer'
import Header from '../components/common/Header'

export default function MainLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8">
        <Outlet />
      </main>
      <Footer />
      <ScrollRestoration />
    </div>
  )
}
