import React from 'react'
import { useQuery } from '@tanstack/react-query'
import api from '../services/api'

function Dashboard() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['stats'],
    queryFn: async () => {
      // TODO: Implement real stats endpoint
      return {
        totalUsers: 0,
        activeUsers: 0,
        totalMatches: 0,
        pendingReports: 0,
        pendingVerifications: 0,
      }
    },
  })

  if (isLoading) {
    return <div className="text-center py-8">Cargando...</div>
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>

      {/* Stats cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-sm font-medium text-gray-500">Usuarios Totales</div>
          <div className="text-3xl font-bold text-gray-900">{stats?.totalUsers || 0}</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-sm font-medium text-gray-500">Usuarios Activos</div>
          <div className="text-3xl font-bold text-green-600">{stats?.activeUsers || 0}</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-sm font-medium text-gray-500">Matches Totales</div>
          <div className="text-3xl font-bold text-pink-600">{stats?.totalMatches || 0}</div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="text-sm font-medium text-gray-500">Reportes Pendientes</div>
          <div className="text-3xl font-bold text-red-600">{stats?.pendingReports || 0}</div>
        </div>
      </div>

      {/* Quick actions */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-semibold mb-4">Acciones Rápidas</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className="px-4 py-2 bg-pink-600 text-white rounded-lg hover:bg-pink-700 transition-colors">
            Verificar Usuarios
          </button>
          <button className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
            Revisar Reportes
          </button>
          <button className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors">
            Gestionar Configuración
          </button>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
