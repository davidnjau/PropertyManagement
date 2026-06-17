import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import DashboardLayout from './components/DashboardLayout'
import TenantLayout from './components/TenantLayout'
import { PaymentMethodsProvider } from './context/PaymentMethodsContext'

const queryClient = new QueryClient()
import Home from './pages/Home'
import Solutions from './pages/Solutions'
import Pricing from './pages/Pricing'
import About from './pages/About'
import Contact from './pages/Contact'
import Auth from './pages/Auth'
import Overview from './pages/dashboard/Overview'
import Buildings from './pages/dashboard/Buildings'
import Tenants from './pages/dashboard/Tenants'
import Payments from './pages/dashboard/Payments'
import Maintenance from './pages/dashboard/Maintenance'
import Documents from './pages/dashboard/Documents'
import PaymentMethodsAdmin from './pages/dashboard/admin/PaymentMethods'
import Alerts from './pages/dashboard/admin/Alerts'
import AdminDocuments from './pages/dashboard/admin/Documents'
import LeaseExtensions from './pages/dashboard/admin/LeaseExtensions'
import TenantOverview from './pages/tenant/Overview'
import PayRent from './pages/tenant/PayRent'
import TenantLease from './pages/tenant/Lease'
import TenantMaintenance from './pages/tenant/Maintenance'
import TenantDocuments from './pages/tenant/Documents'

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
    <PaymentMethodsProvider>
    <BrowserRouter>
      <Routes>
        {/* Auth — no chrome */}
        <Route path="/auth" element={<Auth />} />

        {/* Dashboard — sidebar layout */}
        <Route path="/dashboard" element={<DashboardLayout />}>
          <Route index element={<Overview />} />
          <Route path="buildings" element={<Buildings />} />
          <Route path="tenants" element={<Tenants />} />
          <Route path="payments" element={<Payments />} />
          <Route path="maintenance" element={<Maintenance />} />
          <Route path="documents" element={<Documents />} />
          <Route path="admin/alerts" element={<Alerts />} />
          <Route path="admin/documents" element={<AdminDocuments />} />
          <Route path="admin/payment-methods" element={<PaymentMethodsAdmin />} />
          <Route path="admin/lease-extensions" element={<LeaseExtensions />} />
        </Route>

        {/* Tenant portal — tenant sidebar layout */}
        <Route path="/tenant" element={<TenantLayout />}>
          <Route index element={<TenantOverview />} />
          <Route path="pay-rent" element={<PayRent />} />
          <Route path="lease" element={<TenantLease />} />
          <Route path="maintenance" element={<TenantMaintenance />} />
          <Route path="documents" element={<TenantDocuments />} />
        </Route>

        {/* Marketing — navbar + footer */}
        <Route
          path="/*"
          element={
            <div className="flex flex-col min-h-screen">
              <Navbar />
              <main className="flex-1">
                <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/solutions" element={<Solutions />} />
                  <Route path="/pricing" element={<Pricing />} />
                  <Route path="/about" element={<About />} />
                  <Route path="/contact" element={<Contact />} />
                </Routes>
              </main>
              <Footer />
            </div>
          }
        />
      </Routes>
    </BrowserRouter>
    </PaymentMethodsProvider>
    </QueryClientProvider>
  )
}
