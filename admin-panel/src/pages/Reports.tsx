import React from 'react'
import { useQuery } from '@tanstack/react-query'

function Reports() {
  const { data: reports, isLoading } = useQuery({
    queryKey: ['reports'],
    queryFn: async () => {
      // TODO: Implement real reports endpoint
      return []
    },
  })

  const handleResolveReport = async (reportId: string) => {
    // TODO: Implement resolve report
    alert('Reporte resuelto')
  }

  const handleDismissReport = async (reportId: string) => {
    // TODO: Implement dismiss report
    alert('Reporte desestimado')
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Reportes Pendientes</h1>

      <div className="space-y-4">
        {isLoading ? (
          <div className="bg-white rounded-lg shadow p-6 text-center">Cargando...</div>
        ) : reports?.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-6 text-center text-gray-500">
            No hay reportes pendientes
          </div>
        ) : (
          reports?.map((report: any) => (
            <div key={report.id} className="bg-white rounded-lg shadow p-6">
              <div className="flex justify-between items-start">
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="font-semibold">{report.reporter.username}</span>
                    <span className="text-gray-500">reportó a</span>
                    <span className="font-semibold">{report.reported.username}</span>
                  </div>
                  <div className="mt-2 text-gray-600">
                    <span className="font-medium">Razón:</span> {report.reason}
                  </div>
                  {report.description && (
                    <div className="mt-1 text-gray-500 text-sm">
                      {report.description}
                    </div>
                  )}
                  <div className="mt-2 text-xs text-gray-400">
                    {new Date(report.createdAt).toLocaleString()}
                  </div>
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => handleResolveReport(report.id)}
                    className="px-3 py-1 bg-green-600 text-white text-sm rounded hover:bg-green-700"
                  >
                    Resolver
                  </button>
                  <button
                    onClick={() => handleDismissReport(report.id)}
                    className="px-3 py-1 bg-gray-600 text-white text-sm rounded hover:bg-gray-700"
                  >
                    Desestimar
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default Reports
