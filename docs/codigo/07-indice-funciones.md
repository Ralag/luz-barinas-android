# Índice verificable de funciones

Generado mediante `node docs/codigo/herramientas/inventario.mjs`. No sustituye las explicaciones de los capítulos: sirve para localizar declaraciones y comprobar omisiones.

El inventario reconoce declaraciones Kotlin y Python y usa el parser local de Rolldown para JavaScript/JSX. Incluye funciones locales, componentes, métodos y callbacks con nombre. Los callbacks anónimos se explican con la operación que los contiene. Excluye dependencias de terceros, código generado y archivos de salida.

Las líneas corresponden al código local leído; al cambiarlo, regenera este índice. La detección de nombres no certifica por sí sola la calidad de una explicación. Los archivos marcados como locales no están versionados y su ausencia en un clon no invalida la documentación del producto.

| Capítulo | Declaraciones |
|---|---:|
| [../OPERACION_GATEWAY_SUPABASE.md](../OPERACION_GATEWAY_SUPABASE.md) | 31 |
| [02-android-datos-y-motor.md](02-android-datos-y-motor.md) | 139 |
| [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md) | 255 |
| [04-web-react-y-api.md](04-web-react-y-api.md) | 324 |
| [05-panel-clasico.md](05-panel-clasico.md) | 280 |
| [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md) | 10 |

Archivos examinados: **172**. Declaraciones identificadas: **1039**. Errores de análisis: **0**.

## admin-web/admin.html

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/api/sendPushAlert.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getFirebaseMessaging` | 7 | FunctionDeclaration |
| `readSupabaseConfig` | 19 | FunctionDeclaration |
| `hasSameOrigin` | 36 | FunctionDeclaration |
| `parsePayload` | 51 | FunctionDeclaration |
| `createHandler` | 70 | FunctionDeclaration |
| `sendPushAlert` | 71 | `createHandler` |

## admin-web/app.js

Explicación: [05-panel-clasico.md](05-panel-clasico.md).

| Función | Línea | Contexto |
|---|---:|---|
| `checkAuth` | 111 | FunctionDeclaration |
| `renderAdminAccess` | 116 | FunctionDeclaration |
| `handleLogin` | 140 | FunctionDeclaration |
| `handleLogout` | 153 | FunctionDeclaration |
| `handleAdminAction` | 163 | FunctionDeclaration |
| `editSector` | 169 | `handleAdminAction` |
| `deleteMasterSector` | 170 | `handleAdminAction` |
| `approveCommunityLocation` | 171 | `handleAdminAction` |
| `rejectCommunityLocation` | 172 | `handleAdminAction` |
| `mergeAndApproveCluster` | 173 | `handleAdminAction` |
| `removeSectorFromBlock` | 174 | `handleAdminAction` |
| `reassignSector` | 175 | `handleAdminAction` |
| `updateSectorStatus` | 176 | `handleAdminAction` |
| `focusSectorOnMap` | 177 | `handleAdminAction` |
| `switchTab` | 189 | FunctionDeclaration |
| `initVercelLogsSync` | 218 | FunctionDeclaration |
| `clearVercelConsole` | 267 | FunctionDeclaration |
| `pingVercelApi` | 272 | FunctionDeclaration |
| `updateSyncStatus` | 294 | FunctionDeclaration |
| `initConnectionMonitor` | 312 | FunctionDeclaration |
| `retryFirebaseConnection` | 328 | FunctionDeclaration |
| `initFirebaseSync` | 340 | FunctionDeclaration |
| `fetchPac` | 344 | `initFirebaseSync` |
| `fetchSectors` | 375 | `initFirebaseSync` |
| `fetchCommunity` | 421 | `initFirebaseSync` |
| `fetchCitizen` | 449 | `initFirebaseSync` |
| `fetchBroadcast` | 465 | `initFirebaseSync` |
| `renderMatrixTable` | 488 | FunctionDeclaration |
| `onclick` | 518 | `renderMatrixTable` |
| `getBlockBadgeClass` | 550 | FunctionDeclaration |
| `renderWeeklyAudit` | 563 | FunctionDeclaration |
| `updateDetectedSchemeBadge` | 565 | FunctionDeclaration |
| `openCellModal` | 590 | FunctionDeclaration |
| `closeCellModal` | 612 | FunctionDeclaration |
| `setCellBlock` | 617 | FunctionDeclaration |
| `openBranchGeneratorModal` | 709 | FunctionDeclaration |
| `closeBranchGeneratorModal` | 713 | FunctionDeclaration |
| `handleApplyBranchScale` | 717 | FunctionDeclaration |
| `setPacViewMode` | 787 | FunctionDeclaration |
| `renderMonthlyProjection` | 812 | FunctionDeclaration |
| `applyMonthlyWeekAsBase` | 884 | FunctionDeclaration |
| `openSchemeConfigModal` | 900 | FunctionDeclaration |
| `closeSchemeConfigModal` | 925 | FunctionDeclaration |
| `onSchemeModeChanged` | 929 | FunctionDeclaration |
| `handleApplySchemeConfig` | 943 | FunctionDeclaration |
| `applySingleTurnPreset` | 1015 | FunctionDeclaration |
| `applyDoubleTurnPreset` | 1019 | FunctionDeclaration |
| `publishPacSchedule` | 1023 | FunctionDeclaration |
| `openEditSlotModal` | 1079 | FunctionDeclaration |
| `closeEditSlotModal` | 1089 | FunctionDeclaration |
| `handleSaveSlot` | 1093 | FunctionDeclaration |
| `openAddSlotModal` | 1111 | FunctionDeclaration |
| `closeAddSlotModal` | 1115 | FunctionDeclaration |
| `onNewSlotDurationChanged` | 1119 | FunctionDeclaration |
| `onNewSlotTimeChanged` | 1129 | FunctionDeclaration |
| `handleAddSlot` | 1140 | FunctionDeclaration |
| `deleteSlot` | 1176 | FunctionDeclaration |
| `getActiveBlockNow` | 1192 | FunctionDeclaration |
| `updateTelemetryActiveCut` | 1202 | FunctionDeclaration |
| `applyActivePacCutToSectors` | 1220 | FunctionDeclaration |
| `setTelemetryFilter` | 1263 | FunctionDeclaration |
| `isSectorCommunity` | 1280 | FunctionDeclaration |
| `renderTelemetryList` | 1288 | FunctionDeclaration |
| `updateSectorStatus` | 1390 | FunctionDeclaration |
| `resetAllTelemetryToNormal` | 1403 | FunctionDeclaration |
| `setAllSectorsStatus` | 1425 | FunctionDeclaration |
| `renderBlockKanban` | 1443 | FunctionDeclaration |
| `reassignSector` | 1491 | FunctionDeclaration |
| `removeSectorFromBlock` | 1515 | FunctionDeclaration |
| `openAddSectorToBlockModal` | 1527 | FunctionDeclaration |
| `closeAddSectorModal` | 1531 | FunctionDeclaration |
| `handleAddSectorToBlock` | 1535 | FunctionDeclaration |
| `normalizeSectorText` | 1567 | FunctionDeclaration |
| `getSignificantTokens` | 1583 | FunctionDeclaration |
| `levenshteinDistance` | 1589 | FunctionDeclaration |
| `computeLevenshteinSimilarity` | 1612 | FunctionDeclaration |
| `computeTokenJaccardSimilarity` | 1618 | FunctionDeclaration |
| `computeSectorSimilarity` | 1629 | FunctionDeclaration |
| `analyzeTypoClusters` | 1653 | FunctionDeclaration |
| `updateClusterBadge` | 1721 | FunctionDeclaration |
| `toggleClusteringPanel` | 1726 | FunctionDeclaration |
| `renderTypoClusters` | 1736 | FunctionDeclaration |
| `upsertSectorSafe` | 1815 | FunctionDeclaration |
| `insertApprovedCommunitySector` | 1860 | FunctionDeclaration |
| `mergeAndApproveCluster` | 1870 | FunctionDeclaration |
| `validateWithOSM` | 2010 | FunctionDeclaration |
| `renderOsmResult` | 2065 | FunctionDeclaration |
| `validateMasterSectorsWithOSM` | 2085 | FunctionDeclaration |
| `validateAllWithOSM` | 2173 | FunctionDeclaration |
| `renderCommunityTable` | 2178 | FunctionDeclaration |
| `approveCommunityLocation` | 2246 | FunctionDeclaration |
| `rejectCommunityLocation` | 2313 | FunctionDeclaration |
| `renderActiveBroadcast` | 2331 | FunctionDeclaration |
| `hideActiveBroadcast` | 2373 | FunctionDeclaration |
| `handleDismissBroadcast` | 2378 | FunctionDeclaration |
| `handleSendBroadcast` | 2399 | FunctionDeclaration |
| `testGlobalPushAlert` | 2428 | FunctionDeclaration |
| `setReportFeedFilter` | 2459 | FunctionDeclaration |
| `renderCitizenReportsFeed` | 2475 | FunctionDeclaration |
| `seedAllBarinasCatalog` | 2776 | FunctionDeclaration |
| `saveDonationConfig` | 2827 | FunctionDeclaration |
| `loadDonationUrl` | 2842 | FunctionDeclaration |
| `handleFilterMasterTypeChange` | 2871 | FunctionDeclaration |
| `getSectorCoordinates` | 2880 | FunctionDeclaration |
| `applyBasemap` | 2888 | FunctionDeclaration |
| `changeMapBasemap` | 2947 | FunctionDeclaration |
| `initBarinasMasterMap` | 2951 | FunctionDeclaration |
| `focusBarinasMapHome` | 2995 | FunctionDeclaration |
| `focusSectorOnMap` | 3000 | FunctionDeclaration |
| `updateBarinasMapMarkers` | 3023 | FunctionDeclaration |
| `toggleMapHeatmapMode` | 3070 | FunctionDeclaration |
| `handleMapBlockFilterChange` | 3085 | FunctionDeclaration |
| `initMasterFilterDropdowns` | 3090 | FunctionDeclaration |
| `handleFilterMasterMunChange` | 3115 | FunctionDeclaration |
| `handleFilterMasterChange` | 3136 | FunctionDeclaration |
| `handleSearchMasterInput` | 3144 | FunctionDeclaration |
| `setDirectoryViewMode` | 3149 | FunctionDeclaration |
| `renderGeneralSectorsMaster` | 3165 | FunctionDeclaration |
| `renderWikipediaCardsView` | 3245 | FunctionDeclaration |
| `renderDetailedTableView` | 3366 | FunctionDeclaration |
| `editSector` | 3470 | FunctionDeclaration |
| `openCreateSectorModal` | 3477 | FunctionDeclaration |
| `onclick` | 3559 | `openCreateSectorModal` |
| `closeCreateSectorModal` | 3597 | FunctionDeclaration |
| `handleModalMunicipioChange` | 3602 | FunctionDeclaration |
| `handleGeocodeForManualSector` | 3617 | FunctionDeclaration |
| `handleSaveManualSector` | 3662 | FunctionDeclaration |
| `deleteMasterSector` | 3766 | FunctionDeclaration |
| `openDbMigrationHelper` | 3824 | FunctionDeclaration |
| `closeDbMigrationHelper` | 3832 | FunctionDeclaration |
| `checkSchemaCacheStatus` | 3837 | FunctionDeclaration |
| `copyMigrationSql` | 3897 | FunctionDeclaration |
| `runClientSideLegacyMigration` | 3907 | FunctionDeclaration |

## admin-web/patch_donations.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/public/admin-security.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `installAdminSecurity` | 2 | FunctionExpression |
| `adminSecurityFactory` | 6 | FunctionExpression |
| `escapeHtml` | 9 | `adminSecurityFactory` |
| `normalizeBlock` | 15 | `adminSecurityFactory` |
| `accessError` | 20 | `adminSecurityFactory` |
| `createAdminAuth` | 26 | `adminSecurityFactory` |
| `publish` | 36 | `adminSecurityFactory.createAdminAuth` |
| `clearTimer` | 41 | `adminSecurityFactory.createAdminAuth` |
| `verifiedSession` | 46 | `adminSecurityFactory.createAdminAuth` |
| `requireAdmin` | 69 | `adminSecurityFactory.createAdminAuth` |
| `handleVisibility` | 94 | `adminSecurityFactory.createAdminAuth` |
| `subscribe` | 98 | `adminSecurityFactory.createAdminAuth` |
| `signIn` | 127 | `adminSecurityFactory.createAdminAuth` |
| `signOut` | 139 | `adminSecurityFactory.createAdminAuth` |
| `postJson` | 147 | `adminSecurityFactory.createAdminAuth` |
| `getState` | 167 | `adminSecurityFactory.createAdminAuth` |
| `readPacSchedule` | 170 | `adminSecurityFactory` |

## admin-web/public/app.js

Explicación: [05-panel-clasico.md](05-panel-clasico.md).

| Función | Línea | Contexto |
|---|---:|---|
| `checkAuth` | 111 | FunctionDeclaration |
| `renderAdminAccess` | 116 | FunctionDeclaration |
| `handleLogin` | 140 | FunctionDeclaration |
| `handleLogout` | 153 | FunctionDeclaration |
| `handleAdminAction` | 163 | FunctionDeclaration |
| `editSector` | 169 | `handleAdminAction` |
| `deleteMasterSector` | 170 | `handleAdminAction` |
| `approveCommunityLocation` | 171 | `handleAdminAction` |
| `rejectCommunityLocation` | 172 | `handleAdminAction` |
| `mergeAndApproveCluster` | 173 | `handleAdminAction` |
| `removeSectorFromBlock` | 174 | `handleAdminAction` |
| `reassignSector` | 175 | `handleAdminAction` |
| `updateSectorStatus` | 176 | `handleAdminAction` |
| `focusSectorOnMap` | 177 | `handleAdminAction` |
| `switchTab` | 189 | FunctionDeclaration |
| `initVercelLogsSync` | 218 | FunctionDeclaration |
| `clearVercelConsole` | 267 | FunctionDeclaration |
| `pingVercelApi` | 272 | FunctionDeclaration |
| `updateSyncStatus` | 294 | FunctionDeclaration |
| `initConnectionMonitor` | 312 | FunctionDeclaration |
| `retryFirebaseConnection` | 328 | FunctionDeclaration |
| `initFirebaseSync` | 340 | FunctionDeclaration |
| `fetchPac` | 344 | `initFirebaseSync` |
| `fetchSectors` | 375 | `initFirebaseSync` |
| `fetchCommunity` | 421 | `initFirebaseSync` |
| `fetchCitizen` | 449 | `initFirebaseSync` |
| `fetchBroadcast` | 465 | `initFirebaseSync` |
| `renderMatrixTable` | 488 | FunctionDeclaration |
| `onclick` | 518 | `renderMatrixTable` |
| `getBlockBadgeClass` | 550 | FunctionDeclaration |
| `renderWeeklyAudit` | 563 | FunctionDeclaration |
| `updateDetectedSchemeBadge` | 565 | FunctionDeclaration |
| `openCellModal` | 590 | FunctionDeclaration |
| `closeCellModal` | 612 | FunctionDeclaration |
| `setCellBlock` | 617 | FunctionDeclaration |
| `openBranchGeneratorModal` | 709 | FunctionDeclaration |
| `closeBranchGeneratorModal` | 713 | FunctionDeclaration |
| `handleApplyBranchScale` | 717 | FunctionDeclaration |
| `setPacViewMode` | 787 | FunctionDeclaration |
| `renderMonthlyProjection` | 812 | FunctionDeclaration |
| `applyMonthlyWeekAsBase` | 884 | FunctionDeclaration |
| `openSchemeConfigModal` | 900 | FunctionDeclaration |
| `closeSchemeConfigModal` | 925 | FunctionDeclaration |
| `onSchemeModeChanged` | 929 | FunctionDeclaration |
| `handleApplySchemeConfig` | 943 | FunctionDeclaration |
| `applySingleTurnPreset` | 1015 | FunctionDeclaration |
| `applyDoubleTurnPreset` | 1019 | FunctionDeclaration |
| `publishPacSchedule` | 1023 | FunctionDeclaration |
| `openEditSlotModal` | 1079 | FunctionDeclaration |
| `closeEditSlotModal` | 1089 | FunctionDeclaration |
| `handleSaveSlot` | 1093 | FunctionDeclaration |
| `openAddSlotModal` | 1111 | FunctionDeclaration |
| `closeAddSlotModal` | 1115 | FunctionDeclaration |
| `onNewSlotDurationChanged` | 1119 | FunctionDeclaration |
| `onNewSlotTimeChanged` | 1129 | FunctionDeclaration |
| `handleAddSlot` | 1140 | FunctionDeclaration |
| `deleteSlot` | 1176 | FunctionDeclaration |
| `getActiveBlockNow` | 1192 | FunctionDeclaration |
| `updateTelemetryActiveCut` | 1202 | FunctionDeclaration |
| `applyActivePacCutToSectors` | 1220 | FunctionDeclaration |
| `setTelemetryFilter` | 1263 | FunctionDeclaration |
| `isSectorCommunity` | 1280 | FunctionDeclaration |
| `renderTelemetryList` | 1288 | FunctionDeclaration |
| `updateSectorStatus` | 1390 | FunctionDeclaration |
| `resetAllTelemetryToNormal` | 1403 | FunctionDeclaration |
| `setAllSectorsStatus` | 1425 | FunctionDeclaration |
| `renderBlockKanban` | 1443 | FunctionDeclaration |
| `reassignSector` | 1491 | FunctionDeclaration |
| `removeSectorFromBlock` | 1515 | FunctionDeclaration |
| `openAddSectorToBlockModal` | 1527 | FunctionDeclaration |
| `closeAddSectorModal` | 1531 | FunctionDeclaration |
| `handleAddSectorToBlock` | 1535 | FunctionDeclaration |
| `normalizeSectorText` | 1567 | FunctionDeclaration |
| `getSignificantTokens` | 1583 | FunctionDeclaration |
| `levenshteinDistance` | 1589 | FunctionDeclaration |
| `computeLevenshteinSimilarity` | 1612 | FunctionDeclaration |
| `computeTokenJaccardSimilarity` | 1618 | FunctionDeclaration |
| `computeSectorSimilarity` | 1629 | FunctionDeclaration |
| `analyzeTypoClusters` | 1653 | FunctionDeclaration |
| `updateClusterBadge` | 1721 | FunctionDeclaration |
| `toggleClusteringPanel` | 1726 | FunctionDeclaration |
| `renderTypoClusters` | 1736 | FunctionDeclaration |
| `upsertSectorSafe` | 1815 | FunctionDeclaration |
| `insertApprovedCommunitySector` | 1860 | FunctionDeclaration |
| `mergeAndApproveCluster` | 1870 | FunctionDeclaration |
| `validateWithOSM` | 2010 | FunctionDeclaration |
| `renderOsmResult` | 2065 | FunctionDeclaration |
| `validateMasterSectorsWithOSM` | 2085 | FunctionDeclaration |
| `validateAllWithOSM` | 2173 | FunctionDeclaration |
| `renderCommunityTable` | 2178 | FunctionDeclaration |
| `approveCommunityLocation` | 2246 | FunctionDeclaration |
| `rejectCommunityLocation` | 2313 | FunctionDeclaration |
| `renderActiveBroadcast` | 2331 | FunctionDeclaration |
| `hideActiveBroadcast` | 2373 | FunctionDeclaration |
| `handleDismissBroadcast` | 2378 | FunctionDeclaration |
| `handleSendBroadcast` | 2399 | FunctionDeclaration |
| `testGlobalPushAlert` | 2428 | FunctionDeclaration |
| `setReportFeedFilter` | 2459 | FunctionDeclaration |
| `renderCitizenReportsFeed` | 2475 | FunctionDeclaration |
| `seedAllBarinasCatalog` | 2776 | FunctionDeclaration |
| `saveDonationConfig` | 2827 | FunctionDeclaration |
| `loadDonationUrl` | 2842 | FunctionDeclaration |
| `handleFilterMasterTypeChange` | 2871 | FunctionDeclaration |
| `getSectorCoordinates` | 2880 | FunctionDeclaration |
| `applyBasemap` | 2888 | FunctionDeclaration |
| `changeMapBasemap` | 2947 | FunctionDeclaration |
| `initBarinasMasterMap` | 2951 | FunctionDeclaration |
| `focusBarinasMapHome` | 2995 | FunctionDeclaration |
| `focusSectorOnMap` | 3000 | FunctionDeclaration |
| `updateBarinasMapMarkers` | 3023 | FunctionDeclaration |
| `toggleMapHeatmapMode` | 3070 | FunctionDeclaration |
| `handleMapBlockFilterChange` | 3085 | FunctionDeclaration |
| `initMasterFilterDropdowns` | 3090 | FunctionDeclaration |
| `handleFilterMasterMunChange` | 3115 | FunctionDeclaration |
| `handleFilterMasterChange` | 3136 | FunctionDeclaration |
| `handleSearchMasterInput` | 3144 | FunctionDeclaration |
| `setDirectoryViewMode` | 3149 | FunctionDeclaration |
| `renderGeneralSectorsMaster` | 3165 | FunctionDeclaration |
| `renderWikipediaCardsView` | 3245 | FunctionDeclaration |
| `renderDetailedTableView` | 3366 | FunctionDeclaration |
| `editSector` | 3470 | FunctionDeclaration |
| `openCreateSectorModal` | 3477 | FunctionDeclaration |
| `onclick` | 3559 | `openCreateSectorModal` |
| `closeCreateSectorModal` | 3597 | FunctionDeclaration |
| `handleModalMunicipioChange` | 3602 | FunctionDeclaration |
| `handleGeocodeForManualSector` | 3617 | FunctionDeclaration |
| `handleSaveManualSector` | 3662 | FunctionDeclaration |
| `deleteMasterSector` | 3766 | FunctionDeclaration |
| `openDbMigrationHelper` | 3824 | FunctionDeclaration |
| `closeDbMigrationHelper` | 3832 | FunctionDeclaration |
| `checkSchemaCacheStatus` | 3837 | FunctionDeclaration |
| `copyMigrationSql` | 3897 | FunctionDeclaration |
| `runClientSideLegacyMigration` | 3907 | FunctionDeclaration |

## admin-web/public/pac-map-core.js

Explicación: [05-panel-clasico.md](05-panel-clasico.md).

| Función | Línea | Contexto |
|---|---:|---|
| `installPacMapCore` | 2 | FunctionExpression |
| `createPacMapCore` | 6 | FunctionExpression |
| `textKey` | 32 | `createPacMapCore` |
| `escapeHtml` | 35 | `createPacMapCore` |
| `normalizeBlock` | 38 | `createPacMapCore` |
| `normalizeStatus` | 42 | `createPacMapCore` |
| `statusInfo` | 48 | `createPacMapCore` |
| `validCoordinatePair` | 57 | `createPacMapCore` |
| `coordinates` | 61 | `createPacMapCore` |
| `locationGroups` | 70 | `createPacMapCore` |
| `representativeSector` | 80 | `createPacMapCore` |
| `markerHtml` | 83 | `createPacMapCore` |
| `heatPoints` | 90 | `createPacMapCore` |
| `voltageLabel` | 95 | `createPacMapCore` |

## admin-web/public/pac-schedule-core.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `installPacScheduleCore` | 2 | FunctionExpression |
| `pacScheduleFactory` | 6 | FunctionExpression |
| `civilParts` | 16 | `pacScheduleFactory` |
| `civilEpoch` | 22 | `pacScheduleFactory` |
| `normalizeBlock` | 30 | `pacScheduleFactory` |
| `weekOffset` | 35 | `pacScheduleFactory` |
| `editorOffset` | 37 | `pacScheduleFactory` |
| `nextRevision` | 39 | `pacScheduleFactory` |
| `rotateMatrix` | 44 | `pacScheduleFactory` |
| `validateSchedule` | 49 | `pacScheduleFactory` |
| `windows` | 65 | `pacScheduleFactory` |
| `findWindow` | 86 | `pacScheduleFactory` |
| `monthlyWeeks` | 92 | `pacScheduleFactory` |
| `pacTime` | 100 | `pacScheduleFactory` |

## admin-web/scripts/blog-prerender.mjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `esc` | 28 | ArrowFunctionExpression |
| `jsonLdTag` | 35 | ArrowFunctionExpression |
| `upsertTag` | 38 | FunctionDeclaration |
| `setMeta` | 42 | FunctionDeclaration |
| `applyHead` | 47 | FunctionDeclaration |
| `injectRoot` | 63 | FunctionDeclaration |
| `articleMarkup` | 87 | FunctionDeclaration |
| `blogIndexMarkup` | 118 | FunctionDeclaration |
| `breadcrumb` | 140 | FunctionDeclaration |
| `sitemapXml` | 148 | FunctionDeclaration |
| `blogPrerender` | 173 | FunctionDeclaration |
| `configResolved` | 178 | `blogPrerender` |
| `closeBundle` | 181 | `blogPrerender` |

## admin-web/server/legacy/getMonthlyWeeks.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `exports` | 7 | ArrowFunctionExpression |

## admin-web/server/legacy/getOutagePrediction.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getDayIndex` | 17 | FunctionDeclaration |
| `exports` | 21 | ArrowFunctionExpression |

## admin-web/src/App.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `App` | 17 | FunctionDeclaration |
| `getInitialRoute` | 19 | `App` |
| `handlePopState` | 39 | `App` |
| `navigate` | 47 | `App` |

## admin-web/src/components/blog/ArticleCover.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `ArticleCover` | 13 | FunctionDeclaration |

## admin-web/src/components/blog/EditorialDisclaimer.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `EditorialDisclaimer` | 6 | FunctionDeclaration |

## admin-web/src/components/BlogPreviewSection.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `BlogPreviewSection` | 6 | FunctionDeclaration |
| `navigateTo` | 9 | `BlogPreviewSection` |

## admin-web/src/components/ContactSection.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `ContactSection` | 5 | FunctionDeclaration |
| `handleSubmit` | 15 | `ContactSection` |

## admin-web/src/components/DonationsSection.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `DonationsSection` | 5 | FunctionDeclaration |
| `loadRemoteDonations` | 18 | `DonationsSection` |
| `copyToClipboard` | 39 | `DonationsSection` |

## admin-web/src/components/Features.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `Features` | 4 | FunctionDeclaration |

## admin-web/src/components/Footer.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `Footer` | 4 | FunctionDeclaration |
| `navigateTo` | 5 | `Footer` |

## admin-web/src/components/Hero.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `Hero` | 4 | FunctionDeclaration |

## admin-web/src/components/Navbar.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `Navbar` | 4 | FunctionDeclaration |
| `navigateTo` | 7 | `Navbar` |

## admin-web/src/components/PacLiveWidget.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PacLiveWidget` | 9 | FunctionDeclaration |

## admin-web/src/components/telemetry/CircuitStabilityWidget.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `CircuitStabilityWidget` | 5 | FunctionDeclaration |
| `getStatusConfig` | 41 | `CircuitStabilityWidget` |

## admin-web/src/components/telemetry/CitizenReportsTicker.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `CitizenReportsTicker` | 6 | FunctionDeclaration |
| `getStatusBadge` | 9 | `CitizenReportsTicker` |

## admin-web/src/components/telemetry/LiveBlockCountdown.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getBlockBadgeClass` | 4 | FunctionDeclaration |
| `getBlockBgSoft` | 14 | FunctionDeclaration |
| `LiveBlockCountdown` | 24 | FunctionDeclaration |

## admin-web/src/data/articles.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `stripHtml` | 42 | ArrowFunctionExpression |
| `countWords` | 49 | ArrowFunctionExpression |
| `enrich` | 51 | ArrowFunctionExpression |
| `getArticleBySlug` | 69 | ArrowFunctionExpression |
| `getRelatedArticles` | 71 | ArrowFunctionExpression |
| `getTableOfContents` | 75 | ArrowFunctionExpression |
| `formatDate` | 85 | ArrowFunctionExpression |

## admin-web/src/data/articles/anatomia-pac.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/data/articles/conceptos-energia.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/data/articles/historia-sen-guri.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/data/articles/proteccion-electrodomesticos.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/data/articles/telemetria-ciudadana.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/lib/adminAuth.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/lib/pacScheduleCore.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `installPacScheduleCore` | 2 | FunctionExpression |
| `pacScheduleFactory` | 6 | FunctionExpression |
| `civilParts` | 16 | `pacScheduleFactory` |
| `civilEpoch` | 22 | `pacScheduleFactory` |
| `normalizeBlock` | 30 | `pacScheduleFactory` |
| `weekOffset` | 35 | `pacScheduleFactory` |
| `editorOffset` | 37 | `pacScheduleFactory` |
| `nextRevision` | 39 | `pacScheduleFactory` |
| `rotateMatrix` | 44 | `pacScheduleFactory` |
| `validateSchedule` | 49 | `pacScheduleFactory` |
| `windows` | 65 | `pacScheduleFactory` |
| `findWindow` | 86 | `pacScheduleFactory` |
| `monthlyWeeks` | 92 | `pacScheduleFactory` |
| `pacTime` | 100 | `pacScheduleFactory` |

## admin-web/src/lib/reportSemantics.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `reportStatus` | 5 | FunctionDeclaration |
| `reportPresentation` | 14 | FunctionDeclaration |

## admin-web/src/lib/seo.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `upsert` | 11 | ArrowFunctionExpression |
| `metaByName` | 30 | ArrowFunctionExpression |
| `metaByProperty` | 36 | ArrowFunctionExpression |
| `useDocumentMeta` | 42 | FunctionDeclaration |

## admin-web/src/lib/supabase.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/lib/telemetry.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `formatRelativeTime` | 11 | FunctionDeclaration |
| `calculatePacTime` | 31 | FunctionDeclaration |
| `processCircuitsSummary` | 38 | FunctionDeclaration |
| `fetchTelemetryFromSupabase` | 105 | FunctionDeclaration |
| `useTelemetry` | 159 | FunctionDeclaration |
| `refresh` | 186 | `useTelemetry` |
| `handleVisibilityChange` | 221 | `useTelemetry` |

## admin-web/src/main.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/pages/AdminPortal.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AdminReportsList` | 14 | FunctionDeclaration |
| `AdminPortal` | 53 | FunctionDeclaration |
| `fetchAdminData` | 119 | `AdminPortal` |
| `handleLogin` | 186 | `AdminPortal` |
| `handleLogout` | 200 | `AdminPortal` |
| `cycleCell` | 210 | `AdminPortal` |
| `handleSavePac` | 226 | `AdminPortal` |
| `handleSaveDonations` | 260 | `AdminPortal` |
| `handleAddSector` | 282 | `AdminPortal` |
| `handleRemoveSector` | 292 | `AdminPortal` |
| `handleSendBroadcast` | 300 | `AdminPortal` |
| `pingApi` | 315 | `AdminPortal` |
| `getBadgeClass` | 409 | `AdminPortal` |

## admin-web/src/pages/Blog.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `Blog` | 8 | FunctionDeclaration |
| `open` | 43 | `Blog` |

## admin-web/src/pages/BlogPost.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `BlogPost` | 15 | FunctionDeclaration |
| `handleContentClick` | 60 | `BlogPost` |
| `go` | 70 | `BlogPost` |

## admin-web/src/pages/DeveloperInfo.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `DeveloperInfo` | 4 | FunctionDeclaration |

## admin-web/src/pages/LandingPage.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `LandingPage` | 9 | FunctionDeclaration |

## admin-web/src/pages/PrivacyPolicy.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PrivacyPolicy` | 4 | FunctionDeclaration |

## admin-web/src/pages/RefundPolicy.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `RefundPolicy` | 4 | FunctionDeclaration |

## admin-web/src/pages/TermsConditions.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `TermsConditions` | 4 | FunctionDeclaration |

## admin-web/src/pages/Transparencia.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `Transparencia` | 4 | FunctionDeclaration |

## admin-web/src/pages/tv/AppTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AppTheme` | 17 | FunctionDeclaration |
| `getAppBlockColor` | 32 | `AppTheme` |

## admin-web/src/pages/tv/LightTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `LightTheme` | 13 | FunctionDeclaration |

## admin-web/src/pages/tv/MercuryTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `MercuryTheme` | 12 | FunctionDeclaration |

## admin-web/src/pages/tv/PerformanceTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PerformanceTheme` | 13 | FunctionDeclaration |

## admin-web/src/pages/tv/RadarTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `RadarTheme` | 12 | FunctionDeclaration |

## admin-web/src/pages/tv/WebTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `WebTheme` | 15 | FunctionDeclaration |

## admin-web/src/pages/TvKioskMode.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `TvKioskMode` | 96 | FunctionDeclaration |
| `handleSelectTheme` | 119 | `TvKioskMode` |
| `handleSelectBlock` | 141 | `TvKioskMode` |
| `toggleFullscreen` | 151 | `TvKioskMode` |
| `handleFsChange` | 167 | `TvKioskMode` |
| `requestWakeLock` | 181 | `TvKioskMode` |
| `handleVisibility` | 192 | `TvKioskMode` |
| `handleKeyDown` | 210 | `TvKioskMode` |
| `getCaracasClock` | 265 | `TvKioskMode` |

## admin-web/tests/admin-ui.test.cjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `session` | 9 | ArrowFunctionExpression |
| `fakeClient` | 11 | FunctionDeclaration |
| `authListener` | 14 | `fakeClient` |
| `getSession` | 18 | `fakeClient` |
| `getUser` | 19 | `fakeClient` |
| `refreshSession` | 23 | `fakeClient` |
| `signInWithPassword` | 30 | `fakeClient` |
| `signOut` | 37 | `fakeClient` |
| `onAuthStateChange` | 43 | `fakeClient` |
| `unsubscribe` | 45 | `fakeClient.onAuthStateChange` |
| `authListener` | 45 | `fakeClient.onAuthStateChange.unsubscribe` |
| `rpc` | 48 | `fakeClient` |
| `setRole` | 53 | `fakeClient` |
| `emit` | 53 | `fakeClient` |
| `getItem` | 57 | ArrowFunctionExpression |
| `now` | 60 | ArrowFunctionExpression |
| `now` | 70 | ArrowFunctionExpression |
| `now` | 79 | ArrowFunctionExpression |
| `now` | 87 | ArrowFunctionExpression |
| `now` | 95 | ArrowFunctionExpression |
| `fetch` | 95 | ArrowFunctionExpression |
| `now` | 106 | ArrowFunctionExpression |
| `fetch` | 106 | ArrowFunctionExpression |
| `json` | 108 | `fetch` |
| `now` | 122 | ArrowFunctionExpression |
| `fetch` | 122 | ArrowFunctionExpression |
| `now` | 132 | ArrowFunctionExpression |
| `fetch` | 132 | ArrowFunctionExpression |
| `json` | 132 | `fetch` |
| `now` | 139 | ArrowFunctionExpression |
| `now` | 153 | ArrowFunctionExpression |
| `now` | 163 | ArrowFunctionExpression |
| `fakeNode` | 172 | FunctionDeclaration |
| `add` | 174 | `fakeNode` |
| `remove` | 174 | `fakeNode` |
| `toggle` | 174 | `fakeNode` |
| `appendChild` | 175 | `fakeNode` |
| `setAttribute` | 175 | `fakeNode` |
| `querySelector` | 175 | `fakeNode` |
| `legacyContext` | 178 | FunctionDeclaration |
| `addEventListener` | 180 | `legacyContext` |
| `querySelectorAll` | 180 | `legacyContext` |
| `querySelector` | 180 | `legacyContext` |
| `getElementById` | 181 | `legacyContext` |
| `log` | 183 | `legacyContext` |
| `warn` | 183 | `legacyContext` |
| `error` | 183 | `legacyContext` |
| `setInterval` | 184 | `legacyContext` |
| `alert` | 184 | `legacyContext` |
| `confirm` | 184 | `legacyContext` |
| `getItem` | 185 | `legacyContext` |
| `createClient` | 186 | `legacyContext` |
| `extractFunction` | 194 | FunctionDeclaration |
| `select` | 205 | FunctionExpression |
| `eq` | 205 | FunctionExpression |
| `single` | 205 | ArrowFunctionExpression |
| `upsert` | 205 | FunctionExpression |
| `from` | 206 | ArrowFunctionExpression |
| `publishPacSchedule` | 214 | FunctionExpression |
| `updateSyncStatus` | 214 | ArrowFunctionExpression |
| `from` | 223 | FunctionExpression |
| `select` | 223 | FunctionExpression |
| `eq` | 223 | FunctionExpression |
| `single` | 223 | ArrowFunctionExpression |
| `single` | 226 | ArrowFunctionExpression |
| `getElementById` | 234 | ArrowFunctionExpression |
| `requireAdmin` | 234 | ArrowFunctionExpression |
| `alert` | 236 | FunctionExpression |
| `error` | 236 | FunctionExpression |
| `from` | 237 | FunctionExpression |
| `upsert` | 237 | ArrowFunctionExpression |
| `getElementById` | 254 | ArrowFunctionExpression |
| `requireAdmin` | 254 | ArrowFunctionExpression |
| `alert` | 256 | FunctionExpression |
| `from` | 257 | FunctionExpression |
| `upsert` | 257 | ArrowFunctionExpression |
| `divIcon` | 339 | ArrowFunctionExpression |
| `marker` | 339 | ArrowFunctionExpression |
| `bindPopup` | 339 | `marker` |
| `addTo` | 339 | `marker.bindPopup` |
| `clearLayers` | 340 | FunctionExpression |
| `addLayer` | 340 | FunctionExpression |
| `editSector` | 353 | ArrowFunctionExpression |
| `closest` | 357 | ArrowFunctionExpression |
| `preventDefault` | 357 | FunctionExpression |
| `approvalFixture` | 370 | FunctionDeclaration |
| `randomUUID` | 376 | `approvalFixture` |
| `getItem` | 377 | `approvalFixture` |
| `setItem` | 377 | `approvalFixture` |
| `alert` | 378 | `approvalFixture` |
| `from` | 379 | `approvalFixture` |
| `insert` | 380 | `approvalFixture.from` |
| `upsert` | 381 | `approvalFixture.from` |
| `delete` | 382 | `approvalFixture.from` |
| `eq` | 382 | `approvalFixture.from.delete` |
| `name` | 384 | `approvalFixture` |
| `publishPacSchedule` | 385 | `approvalFixture` |
| `setApprovalFields` | 391 | FunctionDeclaration |
| `alert` | 465 | ArrowFunctionExpression |
| `hideActiveBroadcast` | 466 | ArrowFunctionExpression |
| `from` | 467 | ArrowFunctionExpression |
| `upsert` | 467 | `from` |
| `preventDefault` | 472 | FunctionExpression |

## admin-web/tests/pac-edge-api.test.cjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `json` | 26 | ArrowFunctionExpression |
| `request` | 27 | ArrowFunctionExpression |
| `fixture` | 33 | FunctionDeclaration |
| `now` | 37 | `fixture` |
| `error` | 38 | `fixture` |
| `fetchImpl` | 39 | `fixture` |
| `sendPushImpl` | 52 | `fixture` |
| `fetchOverride` | 76 | ArrowFunctionExpression |
| `fetchOverride` | 93 | ArrowFunctionExpression |
| `fetchOverride` | 112 | ArrowFunctionExpression |
| `fetchOverride` | 196 | ArrowFunctionExpression |
| `fetchImpl` | 215 | ArrowFunctionExpression |
| `error` | 222 | FunctionExpression |
| `fetchImpl` | 222 | ArrowFunctionExpression |
| `fetchImpl` | 235 | ArrowFunctionExpression |
| `now` | 245 | ArrowFunctionExpression |
| `fetchImpl` | 245 | ArrowFunctionExpression |

## admin-web/tests/pac-map.test.cjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `makeMapHarness` | 79 | FunctionDeclaration |
| `element` | 81 | `makeMapHarness` |
| `appendChild` | 82 | `makeMapHarness.element` |
| `addEventListener` | 82 | `makeMapHarness.element` |
| `setAttribute` | 82 | `makeMapHarness.element` |
| `getZoom` | 85 | `makeMapHarness` |
| `setView` | 85 | `makeMapHarness` |
| `fitBounds` | 85 | `makeMapHarness` |
| `removeLayer` | 85 | `makeMapHarness` |
| `invalidateSize` | 85 | `makeMapHarness` |
| `remove` | 85 | `makeMapHarness` |
| `addTo` | 86 | `makeMapHarness` |
| `addLayer` | 86 | `makeMapHarness` |
| `removeLayer` | 86 | `makeMapHarness` |
| `hasLayer` | 86 | `makeMapHarness` |
| `map` | 91 | `makeMapHarness` |
| `layerGroup` | 91 | `makeMapHarness` |
| `divIcon` | 91 | `makeMapHarness` |
| `tileLayer` | 92 | `makeMapHarness` |
| `addTo` | 92 | `makeMapHarness.tileLayer` |
| `on` | 92 | `makeMapHarness.tileLayer` |
| `heatLayer` | 93 | `makeMapHarness` |
| `addTo` | 93 | `makeMapHarness.heatLayer` |
| `marker` | 94 | `makeMapHarness` |
| `setLatLng` | 95 | `makeMapHarness.marker` |
| `setIcon` | 95 | `makeMapHarness.marker` |
| `getPopup` | 95 | `makeMapHarness.marker` |
| `bindPopup` | 95 | `makeMapHarness.marker` |
| `setPopupContent` | 95 | `makeMapHarness.marker` |
| `getLatLng` | 95 | `makeMapHarness.marker` |
| `openPopup` | 95 | `makeMapHarness.marker` |
| `getElementById` | 99 | `makeMapHarness` |
| `onMapReady` | 99 | `makeMapHarness` |
| `onSectorClicked` | 99 | `makeMapHarness` |
| `addEventListener` | 99 | `makeMapHarness` |

## admin-web/tests/pac-schedule.test.cjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/tests/report-aggregation-concurrency.cjs

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

| Función | Línea | Contexto |
|---|---:|---|
| `runSql` | 6 | FunctionDeclaration |
| `main` | 18 | FunctionDeclaration |
| `write` | 21 | `main` |

## admin-web/tests/report-semantics.test.cjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `render` | 74 | ArrowFunctionExpression |
| `getBlockBadgeClass` | 101 | ArrowFunctionExpression |

## admin-web/tests/sendPushAlert.test.cjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `fixture` | 5 | FunctionDeclaration |
| `error` | 9 | `fixture` |
| `createClientImpl` | 10 | `fixture` |
| `getUser` | 13 | `fixture.createClientImpl` |
| `rpc` | 18 | `fixture.createClientImpl` |
| `getMessagingImpl` | 26 | `fixture` |
| `send` | 29 | `fixture.getMessagingImpl` |
| `setHeader` | 44 | `fixture` |
| `status` | 45 | `fixture` |
| `json` | 46 | `fixture` |
| `end` | 47 | `fixture` |
| `run` | 49 | `fixture` |
| `noDelivery` | 52 | FunctionDeclaration |

## admin-web/update_index.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/update_text.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/vite.config.mjs

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `useAppContext` | 17 | Kotlin fun |

## app/src/main/assets/map/map.html

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/assets/map/pac-map-app.js

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `installAndroidMap` | 1 | FunctionExpression |
| `updateStatus` | 9 | `installAndroidMap` |
| `setBasemap` | 16 | `installAndroidMap` |
| `onTileError` | 28 | `installAndroidMap.setBasemap` |
| `selectSector` | 32 | `installAndroidMap` |
| `createPopup` | 36 | `installAndroidMap` |
| `onSelectSector` | 44 | `installAndroidMap.createPopup` |
| `createGroupPopup` | 48 | `installAndroidMap` |
| `appendGroupSector` | 57 | `installAndroidMap.createGroupPopup` |
| `onSelectGroupSector` | 62 | `installAndroidMap.createGroupPopup.appendGroupSector` |
| `renderLayers` | 72 | `installAndroidMap` |
| `removeMissing` | 76 | `installAndroidMap.renderLayers` |
| `updateMarker` | 79 | `installAndroidMap.renderLayers` |
| `updateHeatVisibility` | 101 | `installAndroidMap.renderLayers` |
| `restoreMarker` | 103 | `installAndroidMap.renderLayers` |
| `updateMapData` | 114 | `installAndroidMap` |
| `resumePacMap` | 127 | `installAndroidMap` |
| `disposePacMap` | 128 | `installAndroidMap` |
| `initMap` | 134 | `installAndroidMap` |
| `onBasemapClick` | 140 | `installAndroidMap.initMap` |
| `onHeatmapClick` | 141 | `installAndroidMap.initMap` |
| `onCenterClick` | 142 | `installAndroidMap.initMap` |

## app/src/main/assets/map/pac-map-core.js

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `installPacMapCore` | 2 | FunctionExpression |
| `createPacMapCore` | 6 | FunctionExpression |
| `textKey` | 32 | `createPacMapCore` |
| `escapeHtml` | 35 | `createPacMapCore` |
| `normalizeBlock` | 38 | `createPacMapCore` |
| `normalizeStatus` | 42 | `createPacMapCore` |
| `statusInfo` | 48 | `createPacMapCore` |
| `validCoordinatePair` | 57 | `createPacMapCore` |
| `coordinates` | 61 | `createPacMapCore` |
| `locationGroups` | 70 | `createPacMapCore` |
| `representativeSector` | 80 | `createPacMapCore` |
| `markerHtml` | 83 | `createPacMapCore` |
| `heatPoints` | 90 | `createPacMapCore` |
| `voltageLabel` | 95 | `createPacMapCore` |

## app/src/main/java/com/example/ads/AdFreeRewardCard.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `findAdsActivity` | 34 | Kotlin fun |
| `rememberAdFreeActive` | 42 | Kotlin fun |
| `AdFreeRewardCard` | 69 | Kotlin fun |
| `AdPrivacyOptionsButton` | 118 | Kotlin fun |

## app/src/main/java/com/example/ads/AdFreeRewardStore.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `initialize` | 13 | Kotlin fun |
| `isActive` | 22 | Kotlin fun |
| `grantEarnedReward` | 29 | Kotlin fun |

## app/src/main/java/com/example/ads/AdFreeRewardWindow.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `isActive` | 5 | Kotlin fun |
| `grant` | 13 | Kotlin fun |
| `resolveRewardHours` | 24 | Kotlin fun |
| `resolveAdUnitId` | 27 | Kotlin fun |

## app/src/main/java/com/example/ads/AdsConfiguration.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `unitId` | 9 | Kotlin fun |

## app/src/main/java/com/example/ads/AdsRuntime.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `initialize` | 34 | Kotlin fun |
| `reconcile` | 71 | Kotlin fun |
| `canRequestAds` | 106 | Kotlin fun |
| `showPrivacyOptions` | 109 | Kotlin fun |
| `canShowOn` | 121 | Kotlin fun |
| `canShowAutomaticAd` | 132 | Kotlin fun |
| `acquire` | 137 | Kotlin fun |
| `release` | 143 | Kotlin fun |

## app/src/main/java/com/example/ads/AppOpenInteractionGuard.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `canOpenAfterReturn` | 14 | Kotlin fun |
| `acquire` | 18 | Kotlin fun |
| `release` | 24 | Kotlin fun |
| `AppOpenInteractionProtection` | 32 | Kotlin fun |

## app/src/main/java/com/example/ads/AppOpenLaunchGate.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AppOpenLaunchGate` | 23 | Kotlin fun |

## app/src/main/java/com/example/ads/AppOpenLaunchHost.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AppOpenLaunchHost` | 15 | Kotlin fun |

## app/src/main/java/com/example/ads/AppOpenLaunchPolicy.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `evaluate` | 19 | Kotlin fun |
| `skip` | 54 | Kotlin fun |
| `onAdFinished` | 58 | Kotlin fun |
| `continueOnce` | 61 | Kotlin fun |

## app/src/main/java/com/example/ads/InterstitialPlacementPolicy.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `recordNavigation` | 8 | Kotlin fun |
| `isEligible` | 12 | Kotlin fun |
| `onImpression` | 18 | Kotlin fun |

## app/src/main/java/com/example/ads/RewardedAdManager.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `clear` | 29 | Kotlin fun |
| `load` | 35 | Kotlin fun |
| `onAdLoaded` | 59 | Kotlin fun |
| `onAdFailedToLoad` | 69 | Kotlin fun |
| `show` | 80 | Kotlin fun |
| `finish` | 94 | Kotlin fun |
| `onAdDismissedFullScreenContent` | 109 | Kotlin fun |
| `onAdFailedToShowFullScreenContent` | 110 | Kotlin fun |

## app/src/main/java/com/example/data/local/AppDatabase.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `sectorDao` | 24 | Kotlin fun |
| `outageRecordDao` | 25 | Kotlin fun |
| `pendingReportDao` | 26 | Kotlin fun |
| `migrate` | 33 | Kotlin fun |
| `getInstance` | 39 | Kotlin fun |

## app/src/main/java/com/example/data/local/dao/OutageRecordDao.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getRecentOutagesBySector` | 13 | Kotlin fun |
| `getAllOutages` | 16 | Kotlin fun |
| `insertOutages` | 19 | Kotlin fun |
| `insertOutage` | 22 | Kotlin fun |
| `getCount` | 25 | Kotlin fun |

## app/src/main/java/com/example/data/local/dao/PendingReportDao.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getUnsyncedReports` | 14 | Kotlin fun |
| `getAllReportsFlow` | 17 | Kotlin fun |
| `getUnsyncedReportsCountFlow` | 20 | Kotlin fun |
| `insertReport` | 23 | Kotlin fun |
| `updateReport` | 26 | Kotlin fun |
| `markAsSynced` | 29 | Kotlin fun |
| `deleteOldSyncedReports` | 32 | Kotlin fun |

## app/src/main/java/com/example/data/local/dao/SectorDao.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getAllSectors` | 14 | Kotlin fun |
| `getAllSectorsList` | 17 | Kotlin fun |
| `getSectorById` | 20 | Kotlin fun |
| `observeSectorById` | 23 | Kotlin fun |
| `insertOrUpdateSectors` | 26 | Kotlin fun |
| `insertOrUpdateSector` | 29 | Kotlin fun |
| `updateSector` | 32 | Kotlin fun |
| `getSectorsCount` | 35 | Kotlin fun |

## app/src/main/java/com/example/data/local/entity/OutageRecordEntity.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `toDomain` | 23 | Kotlin fun |

## app/src/main/java/com/example/data/local/entity/PendingReportEntity.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `toDomain` | 24 | Kotlin fun |

## app/src/main/java/com/example/data/local/entity/SectorEntity.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `toDomain` | 22 | Kotlin fun |

## app/src/main/java/com/example/data/model/BarinasLocationsData.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getParroquiasForMunicipality` | 1144 | Kotlin fun |
| `addCustomLocation` | 1153 | Kotlin fun |
| `getAllLocations` | 1158 | Kotlin fun |
| `searchLocations` | 1163 | Kotlin fun |

## app/src/main/java/com/example/data/model/DonationConfig.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/data/model/MonthlyWeeksResponse.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/data/model/PacAlertSettings.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getSettings` | 23 | Kotlin fun |
| `saveSettings` | 36 | Kotlin fun |

## app/src/main/java/com/example/data/model/PacBlock.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `normalize` | 8 | Kotlin fun |

## app/src/main/java/com/example/data/model/PacSchedule.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `updateSlot` | 111 | Kotlin fun |
| `getScheduleSnapshot` | 150 | Kotlin fun |
| `applySnapshot` | 157 | Kotlin fun |
| `getMatrixSnapshot` | 169 | Kotlin fun |
| `getSlotsSnapshot` | 174 | Kotlin fun |
| `updateMatrix` | 179 | Kotlin fun |
| `updateCell` | 189 | Kotlin fun |
| `deleteSlot` | 198 | Kotlin fun |
| `addSlot` | 214 | Kotlin fun |
| `getDayTurnAudit` | 228 | Kotlin fun |
| `deleteTurnAndRebalance` | 254 | Kotlin fun |
| `applySingleTurnPreset` | 321 | Kotlin fun |
| `applyDoubleTurnPreset` | 340 | Kotlin fun |
| `resetToDefault` | 352 | Kotlin fun |
| `addSectorToBlock` | 527 | Kotlin fun |
| `removeSectorFromBlock` | 545 | Kotlin fun |
| `reassignSector` | 556 | Kotlin fun |
| `getMonthlyWeeks` | 577 | Kotlin fun |
| `getDayIndex` | 623 | Kotlin fun |
| `getWeekOffsetForDayOfMonth` | 637 | Kotlin fun |
| `getBlockForDate` | 647 | Kotlin fun |
| `getCurrentSlotIndex` | 655 | Kotlin fun |
| `getActiveBlockNow` | 672 | Kotlin fun |
| `findScheduledWindowForBlock` | 680 | Kotlin fun |
| `findNextWindowForBlock` | 751 | Kotlin fun |

## app/src/main/java/com/example/data/model/PacScheduleSnapshot.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `fingerprint` | 15 | Kotlin fun |
| `parse` | 28 | Kotlin fun |
| `parseRevision` | 61 | Kotlin fun |

## app/src/main/java/com/example/data/model/PowerStatus.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/data/model/ReportSemantics.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `status` | 7 | Kotlin fun |
| `canonicalType` | 13 | Kotlin fun |
| `remoteStatus` | 19 | Kotlin fun |

## app/src/main/java/com/example/data/model/TutorialLink.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `update` | 10 | Kotlin fun |
| `validate` | 12 | Kotlin fun |

## app/src/main/java/com/example/data/PacAlertTargetResolver.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `resolve` | 16 | Kotlin fun |

## app/src/main/java/com/example/data/PacSchedulePrefs.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getPrefs` | 27 | Kotlin fun |
| `saveSchedule` | 31 | Kotlin fun |
| `loadSchedule` | 63 | Kotlin fun |

## app/src/main/java/com/example/data/remote/ApiClient.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/data/remote/dto/TelemetryDtos.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/data/remote/LuzBarinasApi.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getSectors` | 14 | Kotlin fun |
| `getSectorDetails` | 17 | Kotlin fun |
| `submitPowerReport` | 22 | Kotlin fun |
| `getOutagePrediction` | 27 | Kotlin fun |
| `getMonthlyWeeks` | 32 | Kotlin fun |

## app/src/main/java/com/example/data/repository/CloudSyncRepository.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `startRealtimeSync` | 150 | Kotlin fun |
| `handleConfigChange` | 219 | Kotlin fun |
| `processConfig` | 229 | Kotlin fun |
| `listenToSector` | 284 | Kotlin fun |
| `updateLocalSector` | 316 | Kotlin fun |
| `uploadCitizenReport` | 366 | Kotlin fun |
| `fetchWeeklyCitizenReports` | 402 | Kotlin fun |
| `publishPacScheduleToCloud` | 421 | Kotlin fun |
| `uploadCommunityLocation` | 432 | Kotlin fun |
| `applyRemotePacScheduleJson` | 467 | Kotlin fun |
| `stopRealtimeSync` | 493 | Kotlin fun |
| `reportId` | 509 | Kotlin fun |
| `resolvedCoordinates` | 519 | Kotlin fun |

## app/src/main/java/com/example/data/repository/EnergyRepository.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `initializePreloadedDataIfEmpty` | 60 | Kotlin fun |
| `getPredictionForSector` | 149 | Kotlin fun |
| `submitReport` | 165 | Kotlin fun |
| `syncPendingReportsNow` | 260 | Kotlin fun |
| `registerCommunityLocation` | 287 | Kotlin fun |

## app/src/main/java/com/example/engine/OutagePredictionEngine.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `calculateNextOutageWindow` | 17 | Kotlin fun |
| `calculateLocalOutageWindow` | 26 | Kotlin fun |

## app/src/main/java/com/example/glance/LuzBarinasWidget.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `provideGlance` | 47 | Kotlin fun |
| `onAction` | 153 | Kotlin fun |

## app/src/main/java/com/example/MainActivity.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `onStart` | 87 | Kotlin fun |
| `onStop` | 97 | Kotlin fun |
| `onCreate` | 105 | Kotlin fun |
| `onSaveInstanceState` | 147 | Kotlin fun |
| `MainAppScreen` | 155 | Kotlin fun |

## app/src/main/java/com/example/notification/NotificationActionReceiver.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `onReceive` | 22 | Kotlin fun |

## app/src/main/java/com/example/notification/NotificationHelper.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `createNotificationChannel` | 30 | Kotlin fun |
| `showInteractiveOutageAlert` | 84 | Kotlin fun |
| `showConfirmationNotification` | 160 | Kotlin fun |
| `showPredictiveAlert` | 185 | Kotlin fun |
| `showBroadcastNoticeNotification` | 223 | Kotlin fun |

## app/src/main/java/com/example/notification/PacAlarmPlayer.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `startAlarm` | 24 | Kotlin fun |
| `stopAlarm` | 67 | Kotlin fun |
| `isAlarmActive` | 80 | Kotlin fun |

## app/src/main/java/com/example/notification/PacAlarmReceiver.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `onReceive` | 17 | Kotlin fun |
| `handleScheduledAlert` | 38 | Kotlin fun |

## app/src/main/java/com/example/notification/PacAlarmScheduler.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `scheduleNextAlarm` | 36 | Kotlin fun |
| `scheduleNextAlarmAndWait` | 42 | Kotlin fun |
| `beginReschedule` | 48 | Kotlin fun |
| `schedule` | 54 | Kotlin fun |
| `buildPlan` | 76 | Kotlin fun |
| `arm` | 95 | Kotlin fun |
| `cancelAlarms` | 124 | Kotlin fun |
| `cancelPendingAlarms` | 136 | Kotlin fun |

## app/src/main/java/com/example/notification/PacAlertNotifier.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `claim` | 21 | Kotlin fun |
| `showPreAlert` | 28 | Kotlin fun |
| `showRestoreAlert` | 42 | Kotlin fun |
| `showValidationAlert` | 53 | Kotlin fun |
| `isCurrentTarget` | 76 | Kotlin fun |
| `base` | 85 | Kotlin fun |
| `addSiren` | 108 | Kotlin fun |
| `post` | 116 | Kotlin fun |

## app/src/main/java/com/example/ui/components/AdaptiveActionDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AdaptiveActionDialog` | 17 | Kotlin fun |

## app/src/main/java/com/example/ui/components/AdComponents.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `initializeAdMob` | 42 | Kotlin fun |
| `getBannerAdUnitId` | 44 | Kotlin fun |
| `AdaptiveBannerAd` | 48 | Kotlin fun |
| `onAdLoaded` | 63 | Kotlin fun |
| `onAdFailedToLoad` | 64 | Kotlin fun |
| `clear` | 102 | Kotlin fun |
| `showForNavigation` | 104 | Kotlin fun |
| `preload` | 112 | Kotlin fun |
| `onAdLoaded` | 123 | Kotlin fun |
| `onAdFailedToLoad` | 132 | Kotlin fun |
| `showAd` | 143 | Kotlin fun |
| `finish` | 158 | Kotlin fun |
| `onAdShowedFullScreenContent` | 166 | Kotlin fun |
| `onAdDismissedFullScreenContent` | 167 | Kotlin fun |
| `onAdFailedToShowFullScreenContent` | 168 | Kotlin fun |
| `isLaunchEligible` | 197 | Kotlin fun |
| `hasAvailableAd` | 201 | Kotlin fun |
| `clear` | 204 | Kotlin fun |
| `loadAd` | 211 | Kotlin fun |
| `onAdLoaded` | 223 | Kotlin fun |
| `onAdFailedToLoad` | 236 | Kotlin fun |
| `showAdIfAvailable` | 247 | Kotlin fun |
| `finish` | 256 | Kotlin fun |
| `onAdShowedFullScreenContent` | 265 | Kotlin fun |
| `onAdDismissedFullScreenContent` | 269 | Kotlin fun |
| `onAdFailedToShowFullScreenContent` | 270 | Kotlin fun |

## app/src/main/java/com/example/ui/components/BarinasAddressDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `BarinasAddressDialog` | 33 | Kotlin fun |

## app/src/main/java/com/example/ui/components/DonationsDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `DonationsDialog` | 30 | Kotlin fun |

## app/src/main/java/com/example/ui/components/GoogleSearchBar.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `GoogleSearchBar` | 72 | Kotlin fun |

## app/src/main/java/com/example/ui/components/InteractiveBarinasMap.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `mapAssetName` | 42 | Kotlin fun |
| `mapDataScript` | 49 | Kotlin fun |
| `onSectorClicked` | 81 | Kotlin fun |
| `onMapReady` | 87 | Kotlin fun |
| `dispose` | 91 | Kotlin fun |
| `InteractiveBarinasMap` | 99 | Kotlin fun |
| `shouldOverrideUrlLoading` | 150 | Kotlin fun |
| `shouldOverrideUrlLoading` | 153 | Kotlin fun |
| `shouldInterceptRequest` | 155 | Kotlin fun |
| `blockedResponse` | 172 | Kotlin fun |

## app/src/main/java/com/example/ui/components/OnboardingFlowDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `OnboardingFlowDialog` | 40 | Kotlin fun |
| `WelcomeStep` | 91 | Kotlin fun |
| `WelcomeFeature` | 109 | Kotlin fun |
| `PermissionsStep` | 120 | Kotlin fun |
| `BatteryStep` | 148 | Kotlin fun |

## app/src/main/java/com/example/ui/components/PacScheduleView.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PacScheduleView` | 81 | Kotlin fun |
| `PacOfficialMatrixCard` | 624 | Kotlin fun |
| `PacBlockSectorsGrid` | 845 | Kotlin fun |
| `BlockSectorsCard` | 890 | Kotlin fun |
| `getBlockColor` | 1016 | Kotlin fun |

## app/src/main/java/com/example/ui/components/PacSettingsDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PacSettingsDialog` | 45 | Kotlin fun |

## app/src/main/java/com/example/ui/components/StatusBadge.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `StatusBadge` | 33 | Kotlin fun |

## app/src/main/java/com/example/ui/components/ThemeSettingsDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `ThemeSettingsDialog` | 18 | Kotlin fun |
| `ThemeOptionButton` | 151 | Kotlin fun |

## app/src/main/java/com/example/ui/components/UpdatePromptDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `UpdatePromptDialog` | 25 | Kotlin fun |
| `UpdatePromptContent` | 44 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/AdminScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AdminScreen` | 79 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/DashboardScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `DashboardScreen` | 91 | Kotlin fun |
| `BroadcastNoticeBanner` | 802 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/LegalScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `LegalScreen` | 31 | Kotlin fun |
| `PrivacyPolicyContent` | 65 | Kotlin fun |
| `TermsAndConditionsContent` | 100 | Kotlin fun |
| `TransparenciaContent` | 135 | Kotlin fun |
| `DeveloperInfoContent` | 169 | Kotlin fun |
| `RefundPolicyContent` | 301 | Kotlin fun |
| `SystemDocumentationContent` | 322 | Kotlin fun |
| `GovernmentDisclaimerContent` | 349 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/MapScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `MapScreen` | 48 | Kotlin fun |
| `FilterChipItem` | 116 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/SettingsScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `SettingsScreen` | 31 | Kotlin fun |
| `SettingsCategory` | 323 | Kotlin fun |
| `SettingsItem` | 351 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/TelemetryScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `TelemetryScreen` | 56 | Kotlin fun |
| `SummaryStatBox` | 328 | Kotlin fun |

## app/src/main/java/com/example/ui/theme/Color.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/ui/theme/Shape.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/ui/theme/Theme.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `MyApplicationTheme` | 66 | Kotlin fun |

## app/src/main/java/com/example/ui/theme/Type.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## app/src/main/java/com/example/ui/viewmodel/LuzBarinasViewModel.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `selectSector` | 177 | Kotlin fun |
| `switchProfile` | 208 | Kotlin fun |
| `selectSectorByName` | 252 | Kotlin fun |
| `setFilter` | 261 | Kotlin fun |
| `setTab` | 265 | Kotlin fun |
| `toggleDarkMode` | 269 | Kotlin fun |
| `updateTheme` | 275 | Kotlin fun |
| `reportPowerStatus` | 295 | Kotlin fun |
| `syncPendingReportsNow` | 333 | Kotlin fun |
| `triggerPushAlertSimulation` | 350 | Kotlin fun |
| `buildPacStateCopy` | 365 | Kotlin fun |
| `setAdminOpen` | 379 | Kotlin fun |
| `updateMatrixCell` | 383 | Kotlin fun |
| `updateSlot` | 392 | Kotlin fun |
| `deleteSlot` | 401 | Kotlin fun |
| `addSlot` | 412 | Kotlin fun |
| `addSectorToBlock` | 421 | Kotlin fun |
| `removeSectorFromBlock` | 430 | Kotlin fun |
| `reassignSector` | 439 | Kotlin fun |
| `applyDoubleTurnPreset` | 448 | Kotlin fun |
| `applySingleTurnPreset` | 459 | Kotlin fun |
| `resetScheduleToDefault` | 470 | Kotlin fun |
| `deleteTurnAndRebalance` | 474 | Kotlin fun |
| `registerCommunityLocation` | 485 | Kotlin fun |
| `completeOnboarding` | 528 | Kotlin fun |
| `showUserMessage` | 533 | Kotlin fun |
| `setOnboardingOpen` | 535 | Kotlin fun |
| `setUserLocation` | 539 | Kotlin fun |
| `publishScheduleNotification` | 579 | Kotlin fun |
| `dismissBroadcastNotice` | 600 | Kotlin fun |
| `clearUserMessage` | 604 | Kotlin fun |
| `refreshPrediction` | 608 | Kotlin fun |
| `onCleared` | 615 | Kotlin fun |

## app/src/main/java/com/example/utils/AppUpdater.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `checkForUpdates` | 15 | Kotlin fun |
| `openPlayStoreForUpdate` | 71 | Kotlin fun |

## app/src/main/java/com/example/worker/PredictiveNotificationWorker.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `doWork` | 22 | Kotlin fun |
| `enqueue` | 46 | Kotlin fun |

## app/src/main/java/com/example/worker/ReportPowerWorker.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `doWork` | 15 | Kotlin fun |

## app/src/test/java/com/example/AdFreeRewardPersistenceTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `earnedRewardPersistsTheSameWindowUsedByAllAdGatesAndDoesNotExtendOnDuplicateCallback` | 18 | Kotlin fun |

## app/src/test/java/com/example/AdFreeRewardWindowTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `defaultRewardIs12HoursAndConfigurationCannotExceedOneDay` | 14 | Kotlin fun |
| `oneRewardRemovesAdsForExactly24HoursWithoutStacking` | 24 | Kotlin fun |
| `expiredRewardCanBeEarnedAgainAndConfiguredDurationIsBounded` | 33 | Kotlin fun |
| `corruptPreferencesOrClockBeforeTheGrantDoNotCreateUnlimitedAdFreeTime` | 43 | Kotlin fun |
| `debugAlwaysUsesTheOfficialTestUnitForEveryFormatEvenWithProductionConfiguration` | 51 | Kotlin fun |
| `releaseDisablesAFormatUntilAValidProductionUnitIsConfigured` | 60 | Kotlin fun |

## app/src/test/java/com/example/AppOpenLaunchHostTest.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `FakeGate` | 30 | Kotlin fun |
| `returnOverlayPreservesMountedFormAndDraft` | 35 | Kotlin fun |
| `protectedReturnIsSkippedAndNotDeferredUntilFormCloses` | 60 | Kotlin fun |
| `activeAlarmBypassesInitialGateAndAlwaysBlocksReturn` | 91 | Kotlin fun |
| `closingOneProtectedSurfaceCannotReleaseAnother` | 110 | Kotlin fun |

## app/src/test/java/com/example/AppOpenLaunchPolicyTest.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `step` | 8 | Kotlin fun |
| `loadCallbackShowsOnlyWithinLoadingDeadline` | 13 | Kotlin fun |
| `timeoutAndLateCallbackCannotShowOverContent` | 22 | Kotlin fun |
| `skipAndRewardEndLoadingExactlyOnce` | 30 | Kotlin fun |
| `consentOrLoadFailureNeverBlocksEntry` | 38 | Kotlin fun |
| `slowConsentAndSdkDoNotConsumeTheAdLoadingWindow` | 45 | Kotlin fun |
| `initializationHasAHardLimitAndBackgroundCannotShow` | 54 | Kotlin fun |
| `cachedConsentCanPreloadButCannotDisplayBeforeRefreshCompletes` | 63 | Kotlin fun |

## app/src/test/java/com/example/CompactDialogUiTest.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `LargeText` | 38 | Kotlin fun |
| `weeklyMatrixShowsSeventhSlotAndMissingCellsAsUnassigned` | 48 | Kotlin fun |
| `registerSectorSaveRemainsReachableWithLargeText` | 60 | Kotlin fun |
| `updateActionsRemainReachableWithLongReleaseNotesInLandscape` | 85 | Kotlin fun |
| `welcomeCanBeSkippedWithoutOpeningPermissionSetup` | 105 | Kotlin fun |
| `longSectorNameKeepsChangeActionAndIrregularReportUsable` | 120 | Kotlin fun |

## app/src/test/java/com/example/DashboardStatusUiTest.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `publishActivePacForTheWholeDay` | 29 | Kotlin fun |
| `restoreSchedule` | 38 | Kotlin fun |
| `showStatus` | 40 | Kotlin fun |
| `unknownServiceIsNotReportedAsAnObservedOutageDuringPac` | 55 | Kotlin fun |
| `normalCommunityReportRemainsDistinctFromTheActivePac` | 62 | Kotlin fun |
| `irregularFaultKeepsItsLabelDuringAnActiveScheduledCut` | 69 | Kotlin fun |

## app/src/test/java/com/example/ExampleRobolectricTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `read string from context` | 20 | Kotlin fun |
| `verify room sector dao insert and retrieve` | 27 | Kotlin fun |

## app/src/test/java/com/example/ExampleUnitTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `restoreSchedule` | 20 | Kotlin fun |
| `resetSchedule` | 22 | Kotlin fun |
| `test modular arithmetic outage prediction engine` | 25 | Kotlin fun |

## app/src/test/java/com/example/GreetingScreenshotTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `greeting_screenshot` | 25 | Kotlin fun |

## app/src/test/java/com/example/InterstitialPlacementPolicyTest.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `threeTransitions` | 8 | Kotlin fun |
| `timeAndThreeDistinctTransitionsAreBothRequired` | 15 | Kotlin fun |
| `impressionRestartsBothCounterAndThreeMinuteInterval` | 26 | Kotlin fun |
| `anotherFullscreenAdAlsoPreventsBackToBackAds` | 37 | Kotlin fun |

## app/src/test/java/com/example/MapBridgeSecurityTest.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `sector` | 19 | Kotlin fun |
| `selectedIdAndPayloadAreEncodedAsJsonInsteadOfExecutableJavascript` | 26 | Kotlin fun |
| `localAssetOriginOnlyServesAnExplicitAllowlist` | 42 | Kotlin fun |
| `coordinatesAndZeroVoltageArePreservedWhenValid` | 54 | Kotlin fun |

## app/src/test/java/com/example/PacAlertsPersistenceTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `setUp` | 44 | Kotlin fun |
| `tearDown` | 57 | Kotlin fun |
| `insertSector` | 62 | Kotlin fun |
| `selectProfile` | 66 | Kotlin fun |
| `repairsLegacyCachedBlockFromTheSelectedRoomSector` | 78 | Kotlin fun |
| `coldStartLoadsPersistedScheduleBeforeArmingActualAlarmTimes` | 93 | Kotlin fun |
| `olderPersistedScheduleCannotOverrideANewerInMemorySnapshot` | 109 | Kotlin fun |
| `changingProfileReplacesOldAlarmsAndRejectsAnAlreadyQueuedOldIntent` | 122 | Kotlin fun |
| `disabledSettingsCancelPreviouslyScheduledAlarmsAndUnscheduledDArmsNothing` | 151 | Kotlin fun |
| `exactAndPeriodicDeliveryShareADurableKeyButDifferentProfilesAreIndependent` | 169 | Kotlin fun |
| `planHonorsIndependentAlertSwitchesAndSchedulesFutureWhileCurrentCutIsActive` | 179 | Kotlin fun |

## app/src/test/java/com/example/PacScheduleRegressionTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `setUp` | 16 | Kotlin fun |
| `tearDown` | 18 | Kotlin fun |
| `caracas` | 20 | Kotlin fun |
| `normalizesEveryBlockWithoutTakingTheBOfBloque` | 28 | Kotlin fun |
| `findsAnActualWindowForEachOfTheFourBlocks` | 41 | Kotlin fun |
| `missingAndInvalidBlocksNeverInventAnAlarmWindow` | 56 | Kotlin fun |
| `overnightUsesThePreviousStartDateAcrossAMonthBoundaryAndHasExclusiveEnd` | 66 | Kotlin fun |
| `choosesChronologicallyEvenWhenSlotsAreOutOfOrderAndIgnoresDeviceTimezone` | 85 | Kotlin fun |
| `malformedScheduleCannotCreateAWindow` | 102 | Kotlin fun |

## app/src/test/java/com/example/PacScheduleSnapshotTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `source` | 9 | Kotlin fun |
| `isoAndNumericRevisionsRepresentTheSameUpdate` | 14 | Kotlin fun |
| `invalidPayloadCannotPartiallyChangeTheLiveSchedule` | 21 | Kotlin fun |
| `customSlotGapHasNoInventedActiveSlot` | 30 | Kotlin fun |
| `tutorialAllowsOnlyDirectHttpsYoutubeVideoLinks` | 39 | Kotlin fun |
| `reportKindsKeepIrregularSupplyEvenWithPower` | 47 | Kotlin fun |

## patch_appupdater.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_appupdater2.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_cloudsync_fix.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_cloudsync.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_icons_final.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_icons_finally.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_icons.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_icons2.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_imports.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_settings_dialog.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_vm.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## patch_vm2.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## seed_firestore.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

Archivo local de mantenimiento, fuera del seguimiento Git; puede no existir en un clon.

| Función | Línea | Contexto |
|---|---:|---|
| `firestore_patch` | 21 | Python def |
| `firestore_create` | 33 | Python def |
| `to_firestore_value` | 50 | Python def |
| `to_firestore_fields` | 66 | Python def |
| `main` | 236 | Python def |

## seed_supabase.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

Archivo local de mantenimiento, fuera del seguimiento Git; puede no existir en un clon.

| Función | Línea | Contexto |
|---|---:|---|
| `supabase_upsert` | 16 | Python def |
| `main` | 162 | Python def |

## supabase/functions/pac-api/firebase.mjs

Explicación: [../OPERACION_GATEWAY_SUPABASE.md](../OPERACION_GATEWAY_SUPABASE.md).

| Función | Línea | Contexto |
|---|---:|---|
| `base64url` | 7 | FunctionDeclaration |
| `encodeJson` | 13 | FunctionDeclaration |
| `readServiceAccount` | 17 | FunctionDeclaration |
| `createFirebaseSender` | 32 | FunctionDeclaration |
| `sendFirebase` | 34 | `createFirebaseSender` |

## supabase/functions/pac-api/index.ts

Explicación: [../OPERACION_GATEWAY_SUPABASE.md](../OPERACION_GATEWAY_SUPABASE.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## supabase/functions/pac-api/pac-schedule-core.js

Explicación: [../OPERACION_GATEWAY_SUPABASE.md](../OPERACION_GATEWAY_SUPABASE.md).

| Función | Línea | Contexto |
|---|---:|---|
| `installPacScheduleCore` | 2 | FunctionExpression |
| `pacScheduleFactory` | 6 | FunctionExpression |
| `civilParts` | 16 | `pacScheduleFactory` |
| `civilEpoch` | 22 | `pacScheduleFactory` |
| `normalizeBlock` | 30 | `pacScheduleFactory` |
| `weekOffset` | 35 | `pacScheduleFactory` |
| `editorOffset` | 37 | `pacScheduleFactory` |
| `nextRevision` | 39 | `pacScheduleFactory` |
| `rotateMatrix` | 44 | `pacScheduleFactory` |
| `validateSchedule` | 49 | `pacScheduleFactory` |
| `windows` | 65 | `pacScheduleFactory` |
| `findWindow` | 86 | `pacScheduleFactory` |
| `monthlyWeeks` | 92 | `pacScheduleFactory` |
| `pacTime` | 100 | `pacScheduleFactory` |

## supabase/functions/pac-api/router.mjs

Explicación: [../OPERACION_GATEWAY_SUPABASE.md](../OPERACION_GATEWAY_SUPABASE.md).

| Función | Línea | Contexto |
|---|---:|---|
| `parsePushPayload` | 9 | FunctionDeclaration |
| `parseTelemetry` | 23 | FunctionDeclaration |
| `sectorDto` | 38 | FunctionDeclaration |
| `readConfig` | 51 | FunctionDeclaration |
| `routePath` | 66 | FunctionDeclaration |
| `readJson` | 75 | FunctionDeclaration |
| `createPacApi` | 99 | FunctionDeclaration |
| `rest` | 105 | `createPacApi` |
| `schedule` | 121 | `createPacApi` |
| `sector` | 130 | `createPacApi` |
| `pacApi` | 136 | `createPacApi` |
| `respond` | 144 | `createPacApi.pacApi` |

## test.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

Archivo local de mantenimiento, fuera del seguimiento Git; puede no existir en un clon.

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## update_script.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

Archivo local de mantenimiento, fuera del seguimiento Git; puede no existir en un clon.

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.
