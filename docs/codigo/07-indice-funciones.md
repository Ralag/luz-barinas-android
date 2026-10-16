# Índice verificable de funciones

Generado mediante `node docs/codigo/herramientas/inventario.mjs`. No sustituye las explicaciones de los capítulos: sirve para localizar declaraciones y comprobar omisiones.

El inventario reconoce declaraciones Kotlin y Python y usa el parser local de Rolldown para JavaScript/JSX. Incluye funciones locales, componentes, métodos y callbacks con nombre. Los callbacks anónimos se explican con la operación que los contiene. Excluye dependencias de terceros, código generado y archivos de salida.

Las líneas corresponden al código local leído; al cambiarlo, regenera este índice. La detección de nombres no certifica por sí sola la calidad de una explicación. Los archivos marcados como locales no están versionados y su ausencia en un clon no invalida la documentación del producto.

| Capítulo | Declaraciones |
|---|---:|
| [02-android-datos-y-motor.md](02-android-datos-y-motor.md) | 113 |
| [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md) | 131 |
| [04-web-react-y-api.md](04-web-react-y-api.md) | 233 |
| [05-panel-clasico.md](05-panel-clasico.md) | 266 |
| [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md) | 7 |

Archivos examinados: **133**. Declaraciones identificadas: **750**. Errores de análisis: **0**.

## admin-web/admin.html

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/api/getMonthlyWeeks.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `exports` | 7 | ArrowFunctionExpression |

## admin-web/api/getOutagePrediction.js

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `getDayIndex` | 17 | FunctionDeclaration |
| `exports` | 21 | ArrowFunctionExpression |

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
| `checkAuth` | 110 | FunctionDeclaration |
| `renderAdminAccess` | 115 | FunctionDeclaration |
| `handleLogin` | 139 | FunctionDeclaration |
| `handleLogout` | 152 | FunctionDeclaration |
| `handleAdminAction` | 162 | FunctionDeclaration |
| `editSector` | 168 | `handleAdminAction` |
| `deleteMasterSector` | 169 | `handleAdminAction` |
| `approveCommunityLocation` | 170 | `handleAdminAction` |
| `rejectCommunityLocation` | 171 | `handleAdminAction` |
| `mergeAndApproveCluster` | 172 | `handleAdminAction` |
| `removeSectorFromBlock` | 173 | `handleAdminAction` |
| `reassignSector` | 174 | `handleAdminAction` |
| `updateSectorStatus` | 175 | `handleAdminAction` |
| `focusSectorOnMap` | 176 | `handleAdminAction` |
| `switchTab` | 188 | FunctionDeclaration |
| `initVercelLogsSync` | 217 | FunctionDeclaration |
| `clearVercelConsole` | 266 | FunctionDeclaration |
| `pingVercelApi` | 271 | FunctionDeclaration |
| `updateSyncStatus` | 293 | FunctionDeclaration |
| `initConnectionMonitor` | 311 | FunctionDeclaration |
| `retryFirebaseConnection` | 327 | FunctionDeclaration |
| `initFirebaseSync` | 339 | FunctionDeclaration |
| `fetchPac` | 343 | `initFirebaseSync` |
| `fetchSectors` | 383 | `initFirebaseSync` |
| `fetchCommunity` | 429 | `initFirebaseSync` |
| `fetchCitizen` | 457 | `initFirebaseSync` |
| `fetchBroadcast` | 473 | `initFirebaseSync` |
| `renderMatrixTable` | 496 | FunctionDeclaration |
| `onclick` | 526 | `renderMatrixTable` |
| `getBlockBadgeClass` | 558 | FunctionDeclaration |
| `renderWeeklyAudit` | 571 | FunctionDeclaration |
| `updateDetectedSchemeBadge` | 573 | FunctionDeclaration |
| `openCellModal` | 598 | FunctionDeclaration |
| `closeCellModal` | 620 | FunctionDeclaration |
| `setCellBlock` | 625 | FunctionDeclaration |
| `openBranchGeneratorModal` | 717 | FunctionDeclaration |
| `closeBranchGeneratorModal` | 721 | FunctionDeclaration |
| `handleApplyBranchScale` | 725 | FunctionDeclaration |
| `setPacViewMode` | 795 | FunctionDeclaration |
| `renderMonthlyProjection` | 820 | FunctionDeclaration |
| `applyMonthlyWeekAsBase` | 892 | FunctionDeclaration |
| `openSchemeConfigModal` | 908 | FunctionDeclaration |
| `closeSchemeConfigModal` | 933 | FunctionDeclaration |
| `onSchemeModeChanged` | 937 | FunctionDeclaration |
| `handleApplySchemeConfig` | 951 | FunctionDeclaration |
| `applySingleTurnPreset` | 1023 | FunctionDeclaration |
| `applyDoubleTurnPreset` | 1027 | FunctionDeclaration |
| `publishPacSchedule` | 1031 | FunctionDeclaration |
| `openEditSlotModal` | 1083 | FunctionDeclaration |
| `closeEditSlotModal` | 1093 | FunctionDeclaration |
| `handleSaveSlot` | 1097 | FunctionDeclaration |
| `openAddSlotModal` | 1115 | FunctionDeclaration |
| `closeAddSlotModal` | 1119 | FunctionDeclaration |
| `onNewSlotDurationChanged` | 1123 | FunctionDeclaration |
| `onNewSlotTimeChanged` | 1133 | FunctionDeclaration |
| `handleAddSlot` | 1144 | FunctionDeclaration |
| `deleteSlot` | 1180 | FunctionDeclaration |
| `getActiveBlockNow` | 1196 | FunctionDeclaration |
| `updateTelemetryActiveCut` | 1242 | FunctionDeclaration |
| `applyActivePacCutToSectors` | 1260 | FunctionDeclaration |
| `setTelemetryFilter` | 1303 | FunctionDeclaration |
| `isSectorCommunity` | 1320 | FunctionDeclaration |
| `renderTelemetryList` | 1328 | FunctionDeclaration |
| `updateSectorStatus` | 1430 | FunctionDeclaration |
| `resetAllTelemetryToNormal` | 1443 | FunctionDeclaration |
| `setAllSectorsStatus` | 1465 | FunctionDeclaration |
| `renderBlockKanban` | 1483 | FunctionDeclaration |
| `reassignSector` | 1531 | FunctionDeclaration |
| `removeSectorFromBlock` | 1555 | FunctionDeclaration |
| `openAddSectorToBlockModal` | 1567 | FunctionDeclaration |
| `closeAddSectorModal` | 1571 | FunctionDeclaration |
| `handleAddSectorToBlock` | 1575 | FunctionDeclaration |
| `normalizeSectorText` | 1607 | FunctionDeclaration |
| `getSignificantTokens` | 1623 | FunctionDeclaration |
| `levenshteinDistance` | 1629 | FunctionDeclaration |
| `computeLevenshteinSimilarity` | 1652 | FunctionDeclaration |
| `computeTokenJaccardSimilarity` | 1658 | FunctionDeclaration |
| `computeSectorSimilarity` | 1669 | FunctionDeclaration |
| `analyzeTypoClusters` | 1693 | FunctionDeclaration |
| `updateClusterBadge` | 1761 | FunctionDeclaration |
| `toggleClusteringPanel` | 1766 | FunctionDeclaration |
| `renderTypoClusters` | 1776 | FunctionDeclaration |
| `upsertSectorSafe` | 1855 | FunctionDeclaration |
| `insertApprovedCommunitySector` | 1900 | FunctionDeclaration |
| `mergeAndApproveCluster` | 1910 | FunctionDeclaration |
| `validateWithOSM` | 2050 | FunctionDeclaration |
| `renderOsmResult` | 2105 | FunctionDeclaration |
| `validateMasterSectorsWithOSM` | 2125 | FunctionDeclaration |
| `validateAllWithOSM` | 2213 | FunctionDeclaration |
| `renderCommunityTable` | 2218 | FunctionDeclaration |
| `approveCommunityLocation` | 2286 | FunctionDeclaration |
| `rejectCommunityLocation` | 2353 | FunctionDeclaration |
| `renderActiveBroadcast` | 2371 | FunctionDeclaration |
| `hideActiveBroadcast` | 2413 | FunctionDeclaration |
| `handleDismissBroadcast` | 2418 | FunctionDeclaration |
| `handleSendBroadcast` | 2439 | FunctionDeclaration |
| `testGlobalPushAlert` | 2468 | FunctionDeclaration |
| `setReportFeedFilter` | 2499 | FunctionDeclaration |
| `renderCitizenReportsFeed` | 2515 | FunctionDeclaration |
| `seedAllBarinasCatalog` | 2816 | FunctionDeclaration |
| `saveDonationConfig` | 2867 | FunctionDeclaration |
| `loadDonationUrl` | 2882 | FunctionDeclaration |
| `handleFilterMasterTypeChange` | 2911 | FunctionDeclaration |
| `getSectorCoordinates` | 2960 | FunctionDeclaration |
| `applyBasemap` | 2989 | FunctionDeclaration |
| `changeMapBasemap` | 3049 | FunctionDeclaration |
| `initBarinasMasterMap` | 3053 | FunctionDeclaration |
| `focusBarinasMapHome` | 3089 | FunctionDeclaration |
| `focusSectorOnMap` | 3094 | FunctionDeclaration |
| `updateBarinasMapMarkers` | 3115 | FunctionDeclaration |
| `toggleMapHeatmapMode` | 3224 | FunctionDeclaration |
| `handleMapBlockFilterChange` | 3239 | FunctionDeclaration |
| `initMasterFilterDropdowns` | 3244 | FunctionDeclaration |
| `handleFilterMasterMunChange` | 3269 | FunctionDeclaration |
| `handleFilterMasterChange` | 3290 | FunctionDeclaration |
| `handleSearchMasterInput` | 3298 | FunctionDeclaration |
| `setDirectoryViewMode` | 3303 | FunctionDeclaration |
| `renderGeneralSectorsMaster` | 3319 | FunctionDeclaration |
| `renderWikipediaCardsView` | 3399 | FunctionDeclaration |
| `renderDetailedTableView` | 3520 | FunctionDeclaration |
| `editSector` | 3624 | FunctionDeclaration |
| `openCreateSectorModal` | 3631 | FunctionDeclaration |
| `onclick` | 3713 | `openCreateSectorModal` |
| `closeCreateSectorModal` | 3751 | FunctionDeclaration |
| `handleModalMunicipioChange` | 3756 | FunctionDeclaration |
| `handleGeocodeForManualSector` | 3771 | FunctionDeclaration |
| `handleSaveManualSector` | 3816 | FunctionDeclaration |
| `deleteMasterSector` | 3920 | FunctionDeclaration |
| `openDbMigrationHelper` | 3978 | FunctionDeclaration |
| `closeDbMigrationHelper` | 3986 | FunctionDeclaration |
| `checkSchemaCacheStatus` | 3991 | FunctionDeclaration |
| `copyMigrationSql` | 4051 | FunctionDeclaration |
| `runClientSideLegacyMigration` | 4061 | FunctionDeclaration |

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
| `checkAuth` | 110 | FunctionDeclaration |
| `renderAdminAccess` | 115 | FunctionDeclaration |
| `handleLogin` | 139 | FunctionDeclaration |
| `handleLogout` | 152 | FunctionDeclaration |
| `handleAdminAction` | 162 | FunctionDeclaration |
| `editSector` | 168 | `handleAdminAction` |
| `deleteMasterSector` | 169 | `handleAdminAction` |
| `approveCommunityLocation` | 170 | `handleAdminAction` |
| `rejectCommunityLocation` | 171 | `handleAdminAction` |
| `mergeAndApproveCluster` | 172 | `handleAdminAction` |
| `removeSectorFromBlock` | 173 | `handleAdminAction` |
| `reassignSector` | 174 | `handleAdminAction` |
| `updateSectorStatus` | 175 | `handleAdminAction` |
| `focusSectorOnMap` | 176 | `handleAdminAction` |
| `switchTab` | 188 | FunctionDeclaration |
| `initVercelLogsSync` | 217 | FunctionDeclaration |
| `clearVercelConsole` | 266 | FunctionDeclaration |
| `pingVercelApi` | 271 | FunctionDeclaration |
| `updateSyncStatus` | 293 | FunctionDeclaration |
| `initConnectionMonitor` | 311 | FunctionDeclaration |
| `retryFirebaseConnection` | 327 | FunctionDeclaration |
| `initFirebaseSync` | 339 | FunctionDeclaration |
| `fetchPac` | 343 | `initFirebaseSync` |
| `fetchSectors` | 383 | `initFirebaseSync` |
| `fetchCommunity` | 429 | `initFirebaseSync` |
| `fetchCitizen` | 457 | `initFirebaseSync` |
| `fetchBroadcast` | 473 | `initFirebaseSync` |
| `renderMatrixTable` | 496 | FunctionDeclaration |
| `onclick` | 526 | `renderMatrixTable` |
| `getBlockBadgeClass` | 558 | FunctionDeclaration |
| `renderWeeklyAudit` | 571 | FunctionDeclaration |
| `updateDetectedSchemeBadge` | 573 | FunctionDeclaration |
| `openCellModal` | 598 | FunctionDeclaration |
| `closeCellModal` | 620 | FunctionDeclaration |
| `setCellBlock` | 625 | FunctionDeclaration |
| `openBranchGeneratorModal` | 717 | FunctionDeclaration |
| `closeBranchGeneratorModal` | 721 | FunctionDeclaration |
| `handleApplyBranchScale` | 725 | FunctionDeclaration |
| `setPacViewMode` | 795 | FunctionDeclaration |
| `renderMonthlyProjection` | 820 | FunctionDeclaration |
| `applyMonthlyWeekAsBase` | 892 | FunctionDeclaration |
| `openSchemeConfigModal` | 908 | FunctionDeclaration |
| `closeSchemeConfigModal` | 933 | FunctionDeclaration |
| `onSchemeModeChanged` | 937 | FunctionDeclaration |
| `handleApplySchemeConfig` | 951 | FunctionDeclaration |
| `applySingleTurnPreset` | 1023 | FunctionDeclaration |
| `applyDoubleTurnPreset` | 1027 | FunctionDeclaration |
| `publishPacSchedule` | 1031 | FunctionDeclaration |
| `openEditSlotModal` | 1083 | FunctionDeclaration |
| `closeEditSlotModal` | 1093 | FunctionDeclaration |
| `handleSaveSlot` | 1097 | FunctionDeclaration |
| `openAddSlotModal` | 1115 | FunctionDeclaration |
| `closeAddSlotModal` | 1119 | FunctionDeclaration |
| `onNewSlotDurationChanged` | 1123 | FunctionDeclaration |
| `onNewSlotTimeChanged` | 1133 | FunctionDeclaration |
| `handleAddSlot` | 1144 | FunctionDeclaration |
| `deleteSlot` | 1180 | FunctionDeclaration |
| `getActiveBlockNow` | 1196 | FunctionDeclaration |
| `updateTelemetryActiveCut` | 1242 | FunctionDeclaration |
| `applyActivePacCutToSectors` | 1260 | FunctionDeclaration |
| `setTelemetryFilter` | 1303 | FunctionDeclaration |
| `isSectorCommunity` | 1320 | FunctionDeclaration |
| `renderTelemetryList` | 1328 | FunctionDeclaration |
| `updateSectorStatus` | 1430 | FunctionDeclaration |
| `resetAllTelemetryToNormal` | 1443 | FunctionDeclaration |
| `setAllSectorsStatus` | 1465 | FunctionDeclaration |
| `renderBlockKanban` | 1483 | FunctionDeclaration |
| `reassignSector` | 1531 | FunctionDeclaration |
| `removeSectorFromBlock` | 1555 | FunctionDeclaration |
| `openAddSectorToBlockModal` | 1567 | FunctionDeclaration |
| `closeAddSectorModal` | 1571 | FunctionDeclaration |
| `handleAddSectorToBlock` | 1575 | FunctionDeclaration |
| `normalizeSectorText` | 1607 | FunctionDeclaration |
| `getSignificantTokens` | 1623 | FunctionDeclaration |
| `levenshteinDistance` | 1629 | FunctionDeclaration |
| `computeLevenshteinSimilarity` | 1652 | FunctionDeclaration |
| `computeTokenJaccardSimilarity` | 1658 | FunctionDeclaration |
| `computeSectorSimilarity` | 1669 | FunctionDeclaration |
| `analyzeTypoClusters` | 1693 | FunctionDeclaration |
| `updateClusterBadge` | 1761 | FunctionDeclaration |
| `toggleClusteringPanel` | 1766 | FunctionDeclaration |
| `renderTypoClusters` | 1776 | FunctionDeclaration |
| `upsertSectorSafe` | 1855 | FunctionDeclaration |
| `insertApprovedCommunitySector` | 1900 | FunctionDeclaration |
| `mergeAndApproveCluster` | 1910 | FunctionDeclaration |
| `validateWithOSM` | 2050 | FunctionDeclaration |
| `renderOsmResult` | 2105 | FunctionDeclaration |
| `validateMasterSectorsWithOSM` | 2125 | FunctionDeclaration |
| `validateAllWithOSM` | 2213 | FunctionDeclaration |
| `renderCommunityTable` | 2218 | FunctionDeclaration |
| `approveCommunityLocation` | 2286 | FunctionDeclaration |
| `rejectCommunityLocation` | 2353 | FunctionDeclaration |
| `renderActiveBroadcast` | 2371 | FunctionDeclaration |
| `hideActiveBroadcast` | 2413 | FunctionDeclaration |
| `handleDismissBroadcast` | 2418 | FunctionDeclaration |
| `handleSendBroadcast` | 2439 | FunctionDeclaration |
| `testGlobalPushAlert` | 2468 | FunctionDeclaration |
| `setReportFeedFilter` | 2499 | FunctionDeclaration |
| `renderCitizenReportsFeed` | 2515 | FunctionDeclaration |
| `seedAllBarinasCatalog` | 2816 | FunctionDeclaration |
| `saveDonationConfig` | 2867 | FunctionDeclaration |
| `loadDonationUrl` | 2882 | FunctionDeclaration |
| `handleFilterMasterTypeChange` | 2911 | FunctionDeclaration |
| `getSectorCoordinates` | 2960 | FunctionDeclaration |
| `applyBasemap` | 2989 | FunctionDeclaration |
| `changeMapBasemap` | 3049 | FunctionDeclaration |
| `initBarinasMasterMap` | 3053 | FunctionDeclaration |
| `focusBarinasMapHome` | 3089 | FunctionDeclaration |
| `focusSectorOnMap` | 3094 | FunctionDeclaration |
| `updateBarinasMapMarkers` | 3115 | FunctionDeclaration |
| `toggleMapHeatmapMode` | 3224 | FunctionDeclaration |
| `handleMapBlockFilterChange` | 3239 | FunctionDeclaration |
| `initMasterFilterDropdowns` | 3244 | FunctionDeclaration |
| `handleFilterMasterMunChange` | 3269 | FunctionDeclaration |
| `handleFilterMasterChange` | 3290 | FunctionDeclaration |
| `handleSearchMasterInput` | 3298 | FunctionDeclaration |
| `setDirectoryViewMode` | 3303 | FunctionDeclaration |
| `renderGeneralSectorsMaster` | 3319 | FunctionDeclaration |
| `renderWikipediaCardsView` | 3399 | FunctionDeclaration |
| `renderDetailedTableView` | 3520 | FunctionDeclaration |
| `editSector` | 3624 | FunctionDeclaration |
| `openCreateSectorModal` | 3631 | FunctionDeclaration |
| `onclick` | 3713 | `openCreateSectorModal` |
| `closeCreateSectorModal` | 3751 | FunctionDeclaration |
| `handleModalMunicipioChange` | 3756 | FunctionDeclaration |
| `handleGeocodeForManualSector` | 3771 | FunctionDeclaration |
| `handleSaveManualSector` | 3816 | FunctionDeclaration |
| `deleteMasterSector` | 3920 | FunctionDeclaration |
| `openDbMigrationHelper` | 3978 | FunctionDeclaration |
| `closeDbMigrationHelper` | 3986 | FunctionDeclaration |
| `checkSchemaCacheStatus` | 3991 | FunctionDeclaration |
| `copyMigrationSql` | 4051 | FunctionDeclaration |
| `runClientSideLegacyMigration` | 4061 | FunctionDeclaration |

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
| `CitizenReportsTicker` | 5 | FunctionDeclaration |
| `getStatusBadge` | 8 | `CitizenReportsTicker` |

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
| `formatRelativeTime` | 10 | FunctionDeclaration |
| `calculatePacTime` | 30 | FunctionDeclaration |
| `processCircuitsSummary` | 114 | FunctionDeclaration |
| `fetchTelemetryFromSupabase` | 181 | FunctionDeclaration |
| `useTelemetry` | 235 | FunctionDeclaration |
| `refresh` | 262 | `useTelemetry` |
| `handleVisibilityChange` | 297 | `useTelemetry` |

## admin-web/src/main.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/src/pages/AdminPortal.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AdminPortal` | 12 | FunctionDeclaration |
| `fetchAdminData` | 77 | `AdminPortal` |
| `handleLogin` | 141 | `AdminPortal` |
| `handleLogout` | 155 | `AdminPortal` |
| `cycleCell` | 165 | `AdminPortal` |
| `handleSavePac` | 181 | `AdminPortal` |
| `handleSaveDonations` | 211 | `AdminPortal` |
| `handleAddSector` | 233 | `AdminPortal` |
| `handleRemoveSector` | 243 | `AdminPortal` |
| `handleSendBroadcast` | 251 | `AdminPortal` |
| `pingApi` | 266 | `AdminPortal` |
| `getBadgeClass` | 360 | `AdminPortal` |

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
| `AppTheme` | 16 | FunctionDeclaration |
| `getAppBlockColor` | 31 | `AppTheme` |

## admin-web/src/pages/tv/LightTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `LightTheme` | 12 | FunctionDeclaration |

## admin-web/src/pages/tv/MercuryTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `MercuryTheme` | 11 | FunctionDeclaration |

## admin-web/src/pages/tv/PerformanceTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PerformanceTheme` | 12 | FunctionDeclaration |

## admin-web/src/pages/tv/RadarTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `RadarTheme` | 11 | FunctionDeclaration |

## admin-web/src/pages/tv/WebTheme.jsx

Explicación: [04-web-react-y-api.md](04-web-react-y-api.md).

| Función | Línea | Contexto |
|---|---:|---|
| `WebTheme` | 14 | FunctionDeclaration |

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
| `extractFunction` | 193 | FunctionDeclaration |
| `select` | 204 | FunctionExpression |
| `eq` | 204 | FunctionExpression |
| `single` | 204 | ArrowFunctionExpression |
| `upsert` | 204 | FunctionExpression |
| `from` | 205 | ArrowFunctionExpression |
| `publishPacSchedule` | 213 | FunctionExpression |
| `updateSyncStatus` | 213 | ArrowFunctionExpression |
| `from` | 222 | FunctionExpression |
| `select` | 222 | FunctionExpression |
| `eq` | 222 | FunctionExpression |
| `single` | 222 | ArrowFunctionExpression |
| `single` | 225 | ArrowFunctionExpression |
| `getElementById` | 233 | ArrowFunctionExpression |
| `requireAdmin` | 233 | ArrowFunctionExpression |
| `alert` | 235 | FunctionExpression |
| `error` | 235 | FunctionExpression |
| `from` | 236 | FunctionExpression |
| `upsert` | 236 | ArrowFunctionExpression |
| `divIcon` | 287 | ArrowFunctionExpression |
| `marker` | 287 | ArrowFunctionExpression |
| `bindPopup` | 287 | `marker` |
| `clearLayers` | 288 | FunctionExpression |
| `addLayer` | 288 | FunctionExpression |
| `editSector` | 299 | ArrowFunctionExpression |
| `closest` | 303 | ArrowFunctionExpression |
| `preventDefault` | 303 | FunctionExpression |
| `approvalFixture` | 316 | FunctionDeclaration |
| `randomUUID` | 322 | `approvalFixture` |
| `getItem` | 323 | `approvalFixture` |
| `setItem` | 323 | `approvalFixture` |
| `alert` | 324 | `approvalFixture` |
| `from` | 325 | `approvalFixture` |
| `insert` | 326 | `approvalFixture.from` |
| `upsert` | 327 | `approvalFixture.from` |
| `delete` | 328 | `approvalFixture.from` |
| `eq` | 328 | `approvalFixture.from.delete` |
| `name` | 330 | `approvalFixture` |
| `publishPacSchedule` | 331 | `approvalFixture` |
| `setApprovalFields` | 337 | FunctionDeclaration |
| `alert` | 411 | ArrowFunctionExpression |
| `hideActiveBroadcast` | 412 | ArrowFunctionExpression |
| `from` | 413 | ArrowFunctionExpression |
| `upsert` | 413 | `from` |
| `preventDefault` | 418 | FunctionExpression |

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

| Función | Línea | Contexto |
|---|---:|---|
| `initMap` | 265 | FunctionDeclaration |
| `setBasemap` | 283 | FunctionDeclaration |
| `toggleBasemap` | 310 | FunctionDeclaration |
| `toggleHeatmap` | 314 | FunctionDeclaration |
| `recenterBarinas` | 327 | FunctionDeclaration |
| `getCoords` | 333 | FunctionDeclaration |
| `renderLayers` | 358 | FunctionDeclaration |
| `handleSectorClick` | 455 | FunctionDeclaration |
| `updateMapData` | 463 | FunctionExpression |

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
| `updateSlot` | 110 | Kotlin fun |
| `getMatrixSnapshot` | 147 | Kotlin fun |
| `getSlotsSnapshot` | 151 | Kotlin fun |
| `updateMatrix` | 155 | Kotlin fun |
| `updateCell` | 164 | Kotlin fun |
| `deleteSlot` | 172 | Kotlin fun |
| `addSlot` | 187 | Kotlin fun |
| `getDayTurnAudit` | 200 | Kotlin fun |
| `deleteTurnAndRebalance` | 225 | Kotlin fun |
| `applySingleTurnPreset` | 291 | Kotlin fun |
| `applyDoubleTurnPreset` | 309 | Kotlin fun |
| `resetToDefault` | 320 | Kotlin fun |
| `addSectorToBlock` | 494 | Kotlin fun |
| `removeSectorFromBlock` | 511 | Kotlin fun |
| `reassignSector` | 521 | Kotlin fun |
| `getMonthlyWeeks` | 541 | Kotlin fun |
| `getDayIndex` | 586 | Kotlin fun |
| `getWeekOffsetForDayOfMonth` | 599 | Kotlin fun |
| `getBlockForDate` | 608 | Kotlin fun |
| `getCurrentSlotIndex` | 615 | Kotlin fun |
| `getActiveBlockNow` | 638 | Kotlin fun |
| `findScheduledWindowForBlock` | 645 | Kotlin fun |
| `findNextWindowForBlock` | 715 | Kotlin fun |

## app/src/main/java/com/example/data/model/PowerStatus.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

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
| `loadSchedule` | 62 | Kotlin fun |

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
| `startRealtimeSync` | 141 | Kotlin fun |
| `handleConfigChange` | 235 | Kotlin fun |
| `processConfig` | 245 | Kotlin fun |
| `listenToSector` | 297 | Kotlin fun |
| `updateLocalSector` | 329 | Kotlin fun |
| `uploadCitizenReport` | 377 | Kotlin fun |
| `fetchWeeklyCitizenReports` | 411 | Kotlin fun |
| `publishPacScheduleToCloud` | 430 | Kotlin fun |
| `uploadCommunityLocation` | 441 | Kotlin fun |
| `applyRemotePacScheduleJson` | 476 | Kotlin fun |
| `stopRealtimeSync` | 543 | Kotlin fun |

## app/src/main/java/com/example/data/repository/EnergyRepository.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `initializePreloadedDataIfEmpty` | 60 | Kotlin fun |
| `getPredictionForSector` | 210 | Kotlin fun |
| `submitReport` | 226 | Kotlin fun |
| `syncPendingReportsNow` | 326 | Kotlin fun |
| `registerCommunityLocation` | 351 | Kotlin fun |

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
| `onCreate` | 85 | Kotlin fun |
| `MainAppScreen` | 136 | Kotlin fun |

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

## app/src/main/java/com/example/ui/components/AdComponents.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `initializeAdMob` | 21 | Kotlin fun |
| `getBannerAdUnitId` | 28 | Kotlin fun |
| `AdaptiveBannerAd` | 44 | Kotlin fun |
| `preload` | 84 | Kotlin fun |
| `onAdLoaded` | 91 | Kotlin fun |
| `onAdFailedToLoad` | 94 | Kotlin fun |
| `showAd` | 101 | Kotlin fun |
| `onAdDismissedFullScreenContent` | 107 | Kotlin fun |
| `onAdFailedToShowFullScreenContent` | 111 | Kotlin fun |
| `loadAd` | 134 | Kotlin fun |
| `onAdLoaded` | 143 | Kotlin fun |
| `onAdFailedToLoad` | 146 | Kotlin fun |
| `showAdIfAvailable` | 153 | Kotlin fun |
| `onAdDismissedFullScreenContent` | 158 | Kotlin fun |
| `onAdFailedToShowFullScreenContent` | 163 | Kotlin fun |
| `onAdShowedFullScreenContent` | 168 | Kotlin fun |

## app/src/main/java/com/example/ui/components/BarinasAddressDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `BarinasAddressDialog` | 73 | Kotlin fun |

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
| `onSectorClicked` | 39 | Kotlin fun |
| `onMapReady` | 46 | Kotlin fun |
| `InteractiveBarinasMap` | 55 | Kotlin fun |
| `sendDataToMap` | 64 | Kotlin fun |
| `onPageFinished` | 138 | Kotlin fun |

## app/src/main/java/com/example/ui/components/OnboardingFlowDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `OnboardingFlowDialog` | 38 | Kotlin fun |
| `WelcomeStep` | 79 | Kotlin fun |
| `PermissionsStep` | 165 | Kotlin fun |
| `BatteryStep` | 226 | Kotlin fun |

## app/src/main/java/com/example/ui/components/PacScheduleView.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PacScheduleView` | 81 | Kotlin fun |
| `PacOfficialMatrixCard` | 618 | Kotlin fun |
| `PacBlockSectorsGrid` | 839 | Kotlin fun |
| `BlockSectorsCard` | 884 | Kotlin fun |
| `getBlockColor` | 1010 | Kotlin fun |

## app/src/main/java/com/example/ui/components/PacSettingsDialog.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `PacSettingsDialog` | 45 | Kotlin fun |

## app/src/main/java/com/example/ui/components/StatusBadge.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `StatusBadge` | 39 | Kotlin fun |

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
| `UpdatePromptDialog` | 26 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/AdminScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `AdminScreen` | 79 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/DashboardScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `DashboardScreen` | 85 | Kotlin fun |
| `BroadcastNoticeBanner` | 876 | Kotlin fun |

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
| `MapScreen` | 49 | Kotlin fun |
| `FilterChipItem` | 304 | Kotlin fun |

## app/src/main/java/com/example/ui/screens/SettingsScreen.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `SettingsScreen` | 31 | Kotlin fun |
| `SettingsCategory` | 314 | Kotlin fun |
| `SettingsItem` | 342 | Kotlin fun |

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
| `selectSector` | 179 | Kotlin fun |
| `switchProfile` | 210 | Kotlin fun |
| `selectSectorByName` | 254 | Kotlin fun |
| `setFilter` | 263 | Kotlin fun |
| `setTab` | 267 | Kotlin fun |
| `toggleDarkMode` | 271 | Kotlin fun |
| `updateTheme` | 277 | Kotlin fun |
| `reportPowerStatus` | 297 | Kotlin fun |
| `syncPendingReportsNow` | 335 | Kotlin fun |
| `triggerPushAlertSimulation` | 352 | Kotlin fun |
| `buildPacStateCopy` | 367 | Kotlin fun |
| `setAdminOpen` | 381 | Kotlin fun |
| `updateMatrixCell` | 385 | Kotlin fun |
| `updateSlot` | 394 | Kotlin fun |
| `deleteSlot` | 403 | Kotlin fun |
| `addSlot` | 414 | Kotlin fun |
| `addSectorToBlock` | 423 | Kotlin fun |
| `removeSectorFromBlock` | 432 | Kotlin fun |
| `reassignSector` | 441 | Kotlin fun |
| `applyDoubleTurnPreset` | 450 | Kotlin fun |
| `applySingleTurnPreset` | 461 | Kotlin fun |
| `resetScheduleToDefault` | 472 | Kotlin fun |
| `deleteTurnAndRebalance` | 476 | Kotlin fun |
| `registerCommunityLocation` | 487 | Kotlin fun |
| `setOnboardingOpen` | 530 | Kotlin fun |
| `setUserLocation` | 534 | Kotlin fun |
| `publishScheduleNotification` | 574 | Kotlin fun |
| `dismissBroadcastNotice` | 595 | Kotlin fun |
| `clearUserMessage` | 599 | Kotlin fun |
| `refreshPrediction` | 603 | Kotlin fun |
| `onCleared` | 610 | Kotlin fun |

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
| `doWork` | 17 | Kotlin fun |

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
| `changingProfileReplacesOldAlarmsAndRejectsAnAlreadyQueuedOldIntent` | 109 | Kotlin fun |
| `disabledSettingsCancelPreviouslyScheduledAlarmsAndUnscheduledDArmsNothing` | 138 | Kotlin fun |
| `exactAndPeriodicDeliveryShareADurableKeyButDifferentProfilesAreIndependent` | 156 | Kotlin fun |
| `planHonorsIndependentAlertSwitchesAndSchedulesFutureWhileCurrentCutIsActive` | 166 | Kotlin fun |

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

## test.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

Archivo local de mantenimiento, fuera del seguimiento Git; puede no existir en un clon.

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## update_script.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

Archivo local de mantenimiento, fuera del seguimiento Git; puede no existir en un clon.

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.
