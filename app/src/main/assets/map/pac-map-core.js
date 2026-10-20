/* PAC map contract. Keep this file byte-identical to Android assets/map/pac-map-core.js. */
(function installPacMapCore(root, factory) {
  const core = factory();
  if (typeof module === 'object' && module.exports) module.exports = core;
  if (root) root.PacMapCore = core;
})(typeof globalThis !== 'undefined' ? globalThis : this, function createPacMapCore() {
  'use strict';
  const MUNICIPALITIES = Object.freeze({
    'Barinas': [8.6226, -70.2075], 'Bolívar': [8.7561, -70.4078],
    'Antonio José de Sucre': [8.2327, -70.7389], 'Pedraza': [8.3370, -70.5847],
    'Alberto Arvelo Torrealba': [8.7616, -69.9322], 'Cruz Paredes': [8.8105, -70.0768],
    'Ezequiel Zamora': [7.8028, -71.1711], 'Obispos': [8.5912, -70.0728],
    'Rojas': [8.3242, -69.6192], 'Sosa': [8.0645, -69.1764],
    'Andrés Eloy Blanco': [7.5097, -71.2986], 'Arismendi': [8.4870, -68.3411]
  });
  const KNOWN_SECTORS = Object.freeze({
    'alto barinas norte': [8.6180, -70.2350], 'alto barinas sur': [8.6050, -70.2450],
    'alto barinas': [8.6120, -70.2380], 'centro': [8.6275, -70.2050],
    'barinas centro': [8.6275, -70.2050], 'la rosaleda': [8.6320, -70.2280],
    'campo movil': [8.6140, -70.2220], 'los profesionales': [8.6290, -70.2410],
    'don samuel': [8.6410, -70.2150], 'la soledad': [8.6480, -70.2080],
    'mijangos': [8.6190, -70.2010], 'mijagua': [8.6190, -70.2010],
    'ciudad tavacare': [8.5720, -70.2850], 'palacio fajardo': [8.6110, -70.1980],
    'la caramuca': [8.5310, -70.1820], 'el corozo': [8.5020, -70.1710],
    'quebrada seca': [8.7120, -70.2810], 'san silvestre': [8.2710, -70.0120],
    'santa ines': [8.1820, -69.9110], 'barinitas': [8.7561, -70.4078],
    'socopo': [8.2327, -70.7389], 'ciudad bolivia': [8.3370, -70.5847],
    'sabaneta': [8.7616, -69.9322], 'barrancas': [8.8105, -70.0768],
    'santa barbara': [7.8028, -71.1711]
  });
  const BLOCK_COLORS = Object.freeze({ A: '#38bdf8', B: '#f59e0b', C: '#10b981', D: '#f43f5e', '-': '#94a3b8' });
  function textKey(value) {
    return String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
  }
  function escapeHtml(value) {
    return String(value == null ? '' : value).replace(/[&<>"']/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[character]));
  }
  function normalizeBlock(value) {
    const block = String(value || '').toUpperCase().trim().replace(/^BLOQUE\s*/, '').trim();
    return /^[ABCD]$/.test(block) ? block : '-';
  }
  function normalizeStatus(value) {
    const status = String(value || '').toUpperCase().trim();
    if (['IRREGULAR_OUTAGE', 'FALLA_IRREGULAR', 'IRREGULAR', 'AVERIA', 'BAJON'].includes(status)) return 'IRREGULAR_OUTAGE';
    if (['SCHEDULED_OUTAGE', 'SIN_LUZ', 'OUTAGE'].includes(status)) return 'SCHEDULED_OUTAGE';
    return status === 'NORMAL' || status === 'CON_LUZ' ? 'NORMAL' : 'UNKNOWN';
  }
  function statusInfo(value) {
    const status = normalizeStatus(value);
    return {
      NORMAL: { status, label: 'Con luz · registrado', color: '#10b981', symbol: '✓' },
      SCHEDULED_OUTAGE: { status, label: 'Sin luz / corte', color: '#ef4444', symbol: '!' },
      IRREGULAR_OUTAGE: { status, label: 'Falla irregular', color: '#a855f7', symbol: '⚠' },
      UNKNOWN: { status, label: 'Sin estado confirmado', color: '#94a3b8', symbol: '?' }
    }[status];
  }
  function validCoordinatePair(lat, lon) {
    if (lat == null || lon == null || String(lat).trim() === '' || String(lon).trim() === '') return false;
    return Number.isFinite(Number(lat)) && Number.isFinite(Number(lon)) && Number(lat) >= -90 && Number(lat) <= 90 && Number(lon) >= -180 && Number(lon) <= 180 && !(Number(lat) === 0 && Number(lon) === 0);
  }
  function coordinates(sector) {
    if (validCoordinatePair(sector.lat, sector.lon)) return { point: [Number(sector.lat), Number(sector.lon)], approximate: false, label: 'Coordenadas registradas' };
    const name = textKey(sector.name);
    // Longest exact name wins. A name containing "centro" is not enough to locate it.
    const known = Object.keys(KNOWN_SECTORS).sort((a, b) => b.length - a.length).find(key => name === key);
    if (known) return { point: KNOWN_SECTORS[known].slice(), approximate: true, label: 'Referencia aproximada por nombre' };
    const municipality = Object.keys(MUNICIPALITIES).find(key => textKey(key) === textKey(sector.municipio));
    return { point: (MUNICIPALITIES[municipality] || MUNICIPALITIES.Barinas).slice(), approximate: true, label: municipality ? 'Centro municipal aproximado' : 'Sin ubicación precisa · referencia Barinas' };
  }
  function locationGroups(sectors) {
    const groups = new Map();
    sectors.forEach(sector => {
      const location = coordinates(sector);
      const key = location.point.map(value => value.toFixed(6)).join(',');
      if (!groups.has(key)) groups.set(key, { key, point: location.point, sectors: [] });
      groups.get(key).sectors.push(sector);
    });
    return Array.from(groups.values());
  }
  function representativeSector(sectors, selectedId) {
    return sectors.find(sector => sector.id === selectedId) || sectors.find(sector => normalizeStatus(sector.status) === 'IRREGULAR_OUTAGE') || sectors.find(sector => normalizeStatus(sector.status) === 'SCHEDULED_OUTAGE') || sectors[0];
  }
  function markerHtml(sector, selected, count) {
    const block = normalizeBlock(sector.rotationBlock);
    const info = statusInfo(sector.status);
    const approximation = coordinates(sector).approximate;
    const countHtml = count > 1 ? '<span class="pac-map-pin-count">' + Number(count) + '</span>' : '';
    return '<div class="pac-map-pin' + (selected ? ' pac-map-pin-selected' : '') + (approximation ? ' pac-map-pin-approximate' : '') + '" style="--block-color:' + BLOCK_COLORS[block] + ';--status-color:' + info.color + '"><span class="pac-map-pin-block">' + block + '</span><span class="pac-map-pin-state" aria-hidden="true">' + info.symbol + '</span>' + countHtml + '</div>';
  }
  function heatPoints(sectors) {
    // Density of located outage sectors, not fabricated report counts or normal-sector heat.
    return sectors.filter(sector => !coordinates(sector).approximate && ['SCHEDULED_OUTAGE', 'IRREGULAR_OUTAGE'].includes(normalizeStatus(sector.status)))
      .map(sector => { const point = coordinates(sector).point; return [point[0], point[1], 1]; });
  }
  function voltageLabel(value) {
    return value != null && String(value).trim() !== '' && Number.isFinite(Number(value)) && Number(value) >= 0 ? Number(value).toFixed(1) + ' V reportados' : 'Sin tensión reportada';
  }
  return Object.freeze({ MUNICIPALITIES, KNOWN_SECTORS, BLOCK_COLORS, textKey, escapeHtml, normalizeBlock, normalizeStatus, statusInfo, validCoordinatePair, coordinates, locationGroups, representativeSector, markerHtml, heatPoints, voltageLabel });
});
