import React, { useState } from 'react'

function Settings() {
  const [settings, setSettings] = useState({
    minSimilarityScore: 0.6,
    minLivenessScore: 0.7,
    minAntiSpoofingScore: 0.7,
    maxWarningsBeforeRestriction: 3,
    defaultRadiusKm: 50,
    showDistanceByDefault: false,
  })

  const handleSave = () => {
    // TODO: Implement save settings
    alert('Configuración guardada')
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Configuración</h1>

      <div className="bg-white rounded-lg shadow p-6 space-y-6">
        {/* Verification Settings */}
        <div>
          <h2 className="text-lg font-semibold mb-4">Verificación IA</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Similitud Mínima (%)
              </label>
              <input
                type="number"
                value={settings.minSimilarityScore * 100}
                onChange={(e) => setSettings({
                  ...settings,
                  minSimilarityScore: Number(e.target.value) / 100
                })}
                className="w-full px-3 py-2 border rounded-lg"
                min="0"
                max="100"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Liveness Mínimo (%)
              </label>
              <input
                type="number"
                value={settings.minLivenessScore * 100}
                onChange={(e) => setSettings({
                  ...settings,
                  minLivenessScore: Number(e.target.value) / 100
                })}
                className="w-full px-3 py-2 border rounded-lg"
                min="0"
                max="100"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Anti-Spoofing Mínimo (%)
              </label>
              <input
                type="number"
                value={settings.minAntiSpoofingScore * 100}
                onChange={(e) => setSettings({
                  ...settings,
                  minAntiSpoofingScore: Number(e.target.value) / 100
                })}
                className="w-full px-3 py-2 border rounded-lg"
                min="0"
                max="100"
              />
            </div>
          </div>
        </div>

        {/* Moderation Settings */}
        <div>
          <h2 className="text-lg font-semibold mb-4">Moderación</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Máximo de Advertencias
              </label>
              <input
                type="number"
                value={settings.maxWarningsBeforeRestriction}
                onChange={(e) => setSettings({
                  ...settings,
                  maxWarningsBeforeRestriction: Number(e.target.value)
                })}
                className="w-full px-3 py-2 border rounded-lg"
                min="1"
              />
            </div>
          </div>
        </div>

        {/* Location Settings */}
        <div>
          <h2 className="text-lg font-semibold mb-4">Ubicación</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Radio por Defecto (km)
              </label>
              <input
                type="number"
                value={settings.defaultRadiusKm}
                onChange={(e) => setSettings({
                  ...settings,
                  defaultRadiusKm: Number(e.target.value)
                })}
                className="w-full px-3 py-2 border rounded-lg"
                min="1"
              />
            </div>
            <div className="flex items-center">
              <input
                type="checkbox"
                checked={settings.showDistanceByDefault}
                onChange={(e) => setSettings({
                  ...settings,
                  showDistanceByDefault: e.target.checked
                })}
                className="h-4 w-4 text-pink-600 rounded"
              />
              <label className="ml-2 text-sm text-gray-700">
                Mostrar distancia por defecto
              </label>
            </div>
          </div>
        </div>

        <div className="flex justify-end">
          <button
            onClick={handleSave}
            className="px-6 py-2 bg-pink-600 text-white rounded-lg hover:bg-pink-700 transition-colors"
          >
            Guardar Configuración
          </button>
        </div>
      </div>
    </div>
  )
}

export default Settings
