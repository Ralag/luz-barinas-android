(function installAndroidMap() {
  'use strict';
  const core = window.PacMapCore;
  const markers = new Map();
  let map, markerLayer, heatLayer, baseLayer, labelsLayer, resizeObserver;
  let sectors = [], selectedId = null, isSatellite = false, isHeatmap = false, disposed = false;
  let tileError = false;
  const statusElement = document.getElementById('mapStatus');
  function updateStatus() {
    const approximateCount = sectors.filter(sector => core.coordinates(sector).approximate).length;
    const locatedCount = sectors.length - approximateCount;
    statusElement.textContent = tileError ? 'No se pudo cargar el fondo. Los sectores siguen disponibles; comprueba tu conexión.' :
      isHeatmap ? 'Densidad de sectores con cortes o fallas y coordenadas registradas · ' + core.heatPoints(sectors).length + ' ubicados' :
      locatedCount + ' ubicados · ' + approximateCount + ' aproximados (borde punteado). El estado es comunitario.';
  }
  function setBasemap(satellite) {
    isSatellite = satellite;
    tileError = false;
    if (baseLayer) map.removeLayer(baseLayer);
    if (labelsLayer) map.removeLayer(labelsLayer);
    const service = satellite ? 'World_Imagery' : 'Canvas/World_Dark_Gray_Base';
    const reference = satellite ? 'Reference/World_Boundaries_and_Places' : 'Canvas/World_Dark_Gray_Reference';
    baseLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/' + service + '/MapServer/tile/{z}/{y}/{x}', {
      maxNativeZoom: satellite ? 19 : 16, maxZoom: 19,
      attribution: satellite ? 'Tiles © Esri, Maxar, Earthstar Geographics' : 'Tiles © Esri, DeLorme, NAVTEQ'
    }).addTo(map);
    labelsLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/' + reference + '/MapServer/tile/{z}/{y}/{x}', { maxNativeZoom: satellite ? 19 : 16, maxZoom: 19 }).addTo(map);
    baseLayer.on('tileerror', function onTileError() { tileError = true; updateStatus(); });
    document.getElementById('btnBasemap').textContent = satellite ? 'Oscuro' : 'Satélite';
    updateStatus();
  }
  function selectSector(id) {
    if (!sectors.some(sector => sector.id === id)) return;
    if (window.AndroidBridge && typeof window.AndroidBridge.onSectorClicked === 'function') window.AndroidBridge.onSectorClicked(id);
  }
  function createPopup(sector) {
    const info = core.statusInfo(sector.status);
    const popup = document.createElement('div');
    popup.className = 'pac-map-popup';
    popup.innerHTML = '<p class="pac-map-note">Circuito ' + core.escapeHtml(sector.circuitCode || 'sin especificar') + ' · Bloque ' + core.normalizeBlock(sector.rotationBlock) + '</p><h4>' + core.escapeHtml(sector.name) + '</h4><p style="color:' + info.color + '">' + core.escapeHtml(info.label) + '</p><p>' + core.escapeHtml(core.voltageLabel(sector.voltage)) + '</p><p class="pac-map-note">' + core.escapeHtml(core.coordinates(sector).label) + '</p><p class="pac-map-note">' + (sector.isCommunity ? 'Sector comunitario' : 'Sector del directorio') + '</p>';
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = 'Seleccionar sector';
    button.addEventListener('click', function onSelectSector() { selectSector(sector.id); });
    popup.appendChild(button);
    return popup;
  }
  function createGroupPopup(group) {
    if (group.sectors.length === 1) return createPopup(group.sectors[0]);
    const popup = document.createElement('div');
    popup.className = 'pac-map-popup';
    const heading = document.createElement('h4');
    heading.textContent = group.sectors.length + ' sectores en esta referencia';
    popup.appendChild(heading);
    const list = document.createElement('div');
    list.className = 'pac-map-group-list';
    group.sectors.forEach(function appendGroupSector(sector) {
      const button = document.createElement('button');
      const info = core.statusInfo(sector.status);
      button.type = 'button';
      button.textContent = sector.name + ' · ' + core.normalizeBlock(sector.rotationBlock) + ' · ' + info.label;
      button.addEventListener('click', function onSelectGroupSector() { selectSector(sector.id); });
      list.appendChild(button);
    });
    popup.appendChild(list);
    const note = document.createElement('p');
    note.className = 'pac-map-note';
    note.textContent = 'Las referencias aproximadas no señalan los límites ni la dirección de un sector.';
    popup.appendChild(note);
    return popup;
  }
  function renderLayers(selectionChanged) {
    if (!map || disposed) return;
    const groups = core.locationGroups(sectors);
    const ids = new Set(groups.map(group => group.key));
    markers.forEach(function removeMissing(marker, id) {
      if (!ids.has(id)) { markerLayer.removeLayer(marker); markers.delete(id); }
    });
    groups.forEach(function updateMarker(group) {
      const sector = core.representativeSector(group.sectors, selectedId);
      const isSelected = group.sectors.some(item => item.id === selectedId);
      const signature = JSON.stringify(group.sectors) + ':' + (isSelected ? selectedId : '');
      let marker = markers.get(group.key);
      if (!marker) {
        marker = L.marker(group.point, { keyboard: true, title: String(group.sectors.length > 1 ? group.sectors.length + ' sectores: pulsa para elegir' : sector.name || 'Sector') });
        markers.set(group.key, marker);
        markerLayer.addLayer(marker);
      }
      marker.pacSelected = isSelected;
      if (marker.pacSignature !== signature) {
        marker.setLatLng(group.point);
        marker.setIcon(L.divIcon({ className: 'custom-leaflet-marker', html: core.markerHtml(sector, isSelected, group.sectors.length), iconSize: [28, 28], iconAnchor: [14, 14], popupAnchor: [0, -16] }));
        if (marker.getPopup()) marker.setPopupContent(createGroupPopup(group));
        else marker.bindPopup(createGroupPopup(group), { maxWidth: 260 });
        marker.pacSignature = signature;
      }
    });
    if (heatLayer) { map.removeLayer(heatLayer); heatLayer = null; }
    if (isHeatmap && typeof L.heatLayer === 'function') {
      heatLayer = L.heatLayer(core.heatPoints(sectors), { radius: 25, blur: 15, maxZoom: 14, gradient: { .2: '#f59e0b', .6: '#f43f5e', 1: '#ef4444' } }).addTo(map);
      markers.forEach(function updateHeatVisibility(marker) { if (!marker.pacSelected) markerLayer.removeLayer(marker); else markerLayer.addLayer(marker); });
    } else {
      markers.forEach(function restoreMarker(marker) { markerLayer.addLayer(marker); });
    }
    const selectedMarker = Array.from(markers.values()).find(marker => marker.pacSelected);
    if (selectionChanged && selectedMarker) {
      const marker = selectedMarker;
      map.setView(marker.getLatLng(), Math.max(map.getZoom(), 13), { animate: false });
      if (!markerLayer.hasLayer(marker)) markerLayer.addLayer(marker);
      marker.openPopup();
    }
    updateStatus();
  }
  window.updateMapData = function updateMapData(payload, nextSelectedId) {
    if (disposed) return;
    try {
      const data = typeof payload === 'string' ? JSON.parse(payload) : payload;
      if (!Array.isArray(data)) return;
      const seen = new Set();
      sectors = data.filter(sector => sector && typeof sector.id === 'string' && !seen.has(sector.id) && seen.add(sector.id));
      const next = typeof nextSelectedId === 'string' ? nextSelectedId : null;
      const selectionChanged = selectedId !== next;
      selectedId = next;
      renderLayers(selectionChanged);
    } catch (_) { statusElement.textContent = 'No se pudieron representar los sectores. Reabre el mapa.'; }
  };
  window.resumePacMap = function resumePacMap() { if (map && !disposed) map.invalidateSize({ pan: false }); };
  window.disposePacMap = function disposePacMap() {
    disposed = true;
    if (resizeObserver) resizeObserver.disconnect();
    if (map) map.remove();
    markers.clear();
  };
  function initMap() {
    if (map || disposed) return;
    if (typeof L === 'undefined' || !core) { statusElement.textContent = 'No se pudo iniciar el mapa local.'; return; }
    map = L.map('map', { center: core.MUNICIPALITIES.Barinas, zoom: 12, minZoom: 5, maxZoom: 19, zoomControl: true, attributionControl: true });
    markerLayer = L.layerGroup().addTo(map);
    setBasemap(false);
    document.getElementById('btnBasemap').addEventListener('click', function onBasemapClick() { setBasemap(!isSatellite); });
    document.getElementById('btnHeatmap').addEventListener('click', function onHeatmapClick() { isHeatmap = !isHeatmap; this.setAttribute('aria-pressed', String(isHeatmap)); this.textContent = isHeatmap ? 'Pines' : 'Calor'; renderLayers(false); });
    document.getElementById('btnCenter').addEventListener('click', function onCenterClick() {
      const points = sectors.filter(sector => !core.coordinates(sector).approximate).map(sector => core.coordinates(sector).point);
      if (points.length > 1) map.fitBounds(points, { padding: [25, 25], maxZoom: 13 });
      else map.setView(points[0] || core.MUNICIPALITIES.Barinas, 12);
    });
    if (typeof ResizeObserver !== 'undefined') { resizeObserver = new ResizeObserver(window.resumePacMap); resizeObserver.observe(document.getElementById('map')); }
    window.addEventListener('resize', window.resumePacMap);
    renderLayers(false);
    if (window.AndroidBridge && typeof window.AndroidBridge.onMapReady === 'function') window.AndroidBridge.onMapReady();
  }
  initMap();
})();
