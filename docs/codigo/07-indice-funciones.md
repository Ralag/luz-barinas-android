# Índice verificable de funciones

Generado mediante `node docs/codigo/herramientas/inventario.mjs`. No sustituye las explicaciones de los capítulos: sirve para localizar declaraciones y comprobar omisiones.

El inventario reconoce declaraciones Kotlin y Python y usa el parser local de Rolldown para JavaScript/JSX. Incluye funciones locales, componentes, métodos y callbacks con nombre. Los callbacks anónimos se explican con la operación que los contiene. Excluye dependencias de terceros, código generado y archivos de salida.

Las líneas corresponden al código local leído; al cambiarlo, regenera este índice. La detección de nombres no certifica por sí sola la calidad de una explicación.

| Capítulo | Declaraciones |
|---|---:|
| [02-android-datos-y-motor.md](02-android-datos-y-motor.md) | 89 |
| [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md) | 124 |
| [04-web-react-y-api.md](04-web-react-y-api.md) | 101 |
| [05-panel-clasico.md](05-panel-clasico.md) | 242 |
| [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md) | 7 |

Archivos examinados: **124**. Declaraciones identificadas: **563**. Errores de análisis: **0**.

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
| `exports` | 21 | ArrowFunctionExpression |

## admin-web/app.js

Explicación: [05-panel-clasico.md](05-panel-clasico.md).

| Función | Línea | Contexto |
|---|---:|---|
| `checkAuth` | 102 | FunctionDeclaration |
| `handleLogin` | 117 | FunctionDeclaration |
| `handleLogout` | 136 | FunctionDeclaration |
| `switchTab` | 142 | FunctionDeclaration |
| `initVercelLogsSync` | 171 | FunctionDeclaration |
| `clearVercelConsole` | 220 | FunctionDeclaration |
| `pingVercelApi` | 225 | FunctionDeclaration |
| `updateSyncStatus` | 247 | FunctionDeclaration |
| `initConnectionMonitor` | 265 | FunctionDeclaration |
| `retryFirebaseConnection` | 279 | FunctionDeclaration |
| `initFirebaseSync` | 291 | FunctionDeclaration |
| `fetchPac` | 295 | `initFirebaseSync` |
| `fetchSectors` | 335 | `initFirebaseSync` |
| `fetchCommunity` | 385 | `initFirebaseSync` |
| `fetchCitizen` | 413 | `initFirebaseSync` |
| `fetchBroadcast` | 429 | `initFirebaseSync` |
| `renderMatrixTable` | 452 | FunctionDeclaration |
| `onclick` | 482 | `renderMatrixTable` |
| `getBlockBadgeClass` | 514 | FunctionDeclaration |
| `renderWeeklyAudit` | 527 | FunctionDeclaration |
| `updateDetectedSchemeBadge` | 529 | FunctionDeclaration |
| `openCellModal` | 554 | FunctionDeclaration |
| `closeCellModal` | 576 | FunctionDeclaration |
| `setCellBlock` | 581 | FunctionDeclaration |
| `openBranchGeneratorModal` | 673 | FunctionDeclaration |
| `closeBranchGeneratorModal` | 677 | FunctionDeclaration |
| `handleApplyBranchScale` | 681 | FunctionDeclaration |
| `setPacViewMode` | 752 | FunctionDeclaration |
| `renderMonthlyProjection` | 777 | FunctionDeclaration |
| `applyMonthlyWeekAsBase` | 849 | FunctionDeclaration |
| `openSchemeConfigModal` | 866 | FunctionDeclaration |
| `closeSchemeConfigModal` | 891 | FunctionDeclaration |
| `onSchemeModeChanged` | 895 | FunctionDeclaration |
| `handleApplySchemeConfig` | 909 | FunctionDeclaration |
| `applySingleTurnPreset` | 983 | FunctionDeclaration |
| `applyDoubleTurnPreset` | 987 | FunctionDeclaration |
| `publishPacSchedule` | 991 | FunctionDeclaration |
| `openEditSlotModal` | 1037 | FunctionDeclaration |
| `closeEditSlotModal` | 1047 | FunctionDeclaration |
| `handleSaveSlot` | 1051 | FunctionDeclaration |
| `openAddSlotModal` | 1069 | FunctionDeclaration |
| `closeAddSlotModal` | 1073 | FunctionDeclaration |
| `onNewSlotDurationChanged` | 1077 | FunctionDeclaration |
| `onNewSlotTimeChanged` | 1087 | FunctionDeclaration |
| `handleAddSlot` | 1098 | FunctionDeclaration |
| `deleteSlot` | 1136 | FunctionDeclaration |
| `getActiveBlockNow` | 1152 | FunctionDeclaration |
| `updateTelemetryActiveCut` | 1198 | FunctionDeclaration |
| `applyActivePacCutToSectors` | 1216 | FunctionDeclaration |
| `setTelemetryFilter` | 1259 | FunctionDeclaration |
| `renderTelemetryList` | 1276 | FunctionDeclaration |
| `isSectorCommunity` | 1306 | `renderTelemetryList` |
| `updateSectorStatus` | 1385 | FunctionDeclaration |
| `resetAllTelemetryToNormal` | 1398 | FunctionDeclaration |
| `setAllSectorsStatus` | 1420 | FunctionDeclaration |
| `renderBlockKanban` | 1438 | FunctionDeclaration |
| `reassignSector` | 1486 | FunctionDeclaration |
| `removeSectorFromBlock` | 1510 | FunctionDeclaration |
| `openAddSectorToBlockModal` | 1522 | FunctionDeclaration |
| `closeAddSectorModal` | 1526 | FunctionDeclaration |
| `handleAddSectorToBlock` | 1530 | FunctionDeclaration |
| `normalizeSectorText` | 1562 | FunctionDeclaration |
| `getSignificantTokens` | 1578 | FunctionDeclaration |
| `levenshteinDistance` | 1584 | FunctionDeclaration |
| `computeLevenshteinSimilarity` | 1607 | FunctionDeclaration |
| `computeTokenJaccardSimilarity` | 1613 | FunctionDeclaration |
| `computeSectorSimilarity` | 1624 | FunctionDeclaration |
| `analyzeTypoClusters` | 1648 | FunctionDeclaration |
| `updateClusterBadge` | 1714 | FunctionDeclaration |
| `toggleClusteringPanel` | 1719 | FunctionDeclaration |
| `renderTypoClusters` | 1729 | FunctionDeclaration |
| `upsertSectorSafe` | 1808 | FunctionDeclaration |
| `mergeAndApproveCluster` | 1853 | FunctionDeclaration |
| `validateWithOSM` | 1992 | FunctionDeclaration |
| `renderOsmResult` | 2047 | FunctionDeclaration |
| `validateMasterSectorsWithOSM` | 2067 | FunctionDeclaration |
| `validateAllWithOSM` | 2155 | FunctionDeclaration |
| `renderCommunityTable` | 2160 | FunctionDeclaration |
| `approveCommunityLocation` | 2228 | FunctionDeclaration |
| `rejectCommunityLocation` | 2331 | FunctionDeclaration |
| `renderActiveBroadcast` | 2349 | FunctionDeclaration |
| `hideActiveBroadcast` | 2391 | FunctionDeclaration |
| `handleDismissBroadcast` | 2396 | FunctionDeclaration |
| `handleSendBroadcast` | 2415 | FunctionDeclaration |
| `testGlobalPushAlert` | 2442 | FunctionDeclaration |
| `setReportFeedFilter` | 2479 | FunctionDeclaration |
| `renderCitizenReportsFeed` | 2495 | FunctionDeclaration |
| `seedAllBarinasCatalog` | 2796 | FunctionDeclaration |
| `saveDonationConfig` | 2847 | FunctionDeclaration |
| `loadDonationUrl` | 2862 | FunctionDeclaration |
| `handleFilterMasterTypeChange` | 2891 | FunctionDeclaration |
| `getSectorCoordinates` | 2940 | FunctionDeclaration |
| `applyBasemap` | 2969 | FunctionDeclaration |
| `changeMapBasemap` | 3029 | FunctionDeclaration |
| `initBarinasMasterMap` | 3033 | FunctionDeclaration |
| `focusBarinasMapHome` | 3069 | FunctionDeclaration |
| `focusSectorOnMap` | 3074 | FunctionDeclaration |
| `updateBarinasMapMarkers` | 3095 | FunctionDeclaration |
| `toggleMapHeatmapMode` | 3204 | FunctionDeclaration |
| `handleMapBlockFilterChange` | 3219 | FunctionDeclaration |
| `initMasterFilterDropdowns` | 3224 | FunctionDeclaration |
| `handleFilterMasterMunChange` | 3249 | FunctionDeclaration |
| `handleFilterMasterChange` | 3270 | FunctionDeclaration |
| `handleSearchMasterInput` | 3278 | FunctionDeclaration |
| `setDirectoryViewMode` | 3283 | FunctionDeclaration |
| `renderGeneralSectorsMaster` | 3299 | FunctionDeclaration |
| `renderWikipediaCardsView` | 3379 | FunctionDeclaration |
| `renderDetailedTableView` | 3500 | FunctionDeclaration |
| `editSector` | 3604 | FunctionDeclaration |
| `openCreateSectorModal` | 3611 | FunctionDeclaration |
| `onclick` | 3693 | `openCreateSectorModal` |
| `closeCreateSectorModal` | 3731 | FunctionDeclaration |
| `handleModalMunicipioChange` | 3736 | FunctionDeclaration |
| `handleGeocodeForManualSector` | 3751 | FunctionDeclaration |
| `handleSaveManualSector` | 3796 | FunctionDeclaration |
| `deleteMasterSector` | 3900 | FunctionDeclaration |
| `openDbMigrationHelper` | 3958 | FunctionDeclaration |
| `closeDbMigrationHelper` | 3966 | FunctionDeclaration |
| `checkSchemaCacheStatus` | 3971 | FunctionDeclaration |
| `copyMigrationSql` | 4031 | FunctionDeclaration |
| `runClientSideLegacyMigration` | 4082 | FunctionDeclaration |

## admin-web/patch_donations.js

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## admin-web/public/app.js

Explicación: [05-panel-clasico.md](05-panel-clasico.md).

| Función | Línea | Contexto |
|---|---:|---|
| `checkAuth` | 102 | FunctionDeclaration |
| `handleLogin` | 117 | FunctionDeclaration |
| `handleLogout` | 136 | FunctionDeclaration |
| `switchTab` | 142 | FunctionDeclaration |
| `initVercelLogsSync` | 171 | FunctionDeclaration |
| `clearVercelConsole` | 220 | FunctionDeclaration |
| `pingVercelApi` | 225 | FunctionDeclaration |
| `updateSyncStatus` | 247 | FunctionDeclaration |
| `initConnectionMonitor` | 265 | FunctionDeclaration |
| `retryFirebaseConnection` | 279 | FunctionDeclaration |
| `initFirebaseSync` | 291 | FunctionDeclaration |
| `fetchPac` | 295 | `initFirebaseSync` |
| `fetchSectors` | 335 | `initFirebaseSync` |
| `fetchCommunity` | 385 | `initFirebaseSync` |
| `fetchCitizen` | 413 | `initFirebaseSync` |
| `fetchBroadcast` | 429 | `initFirebaseSync` |
| `renderMatrixTable` | 452 | FunctionDeclaration |
| `onclick` | 482 | `renderMatrixTable` |
| `getBlockBadgeClass` | 514 | FunctionDeclaration |
| `renderWeeklyAudit` | 527 | FunctionDeclaration |
| `updateDetectedSchemeBadge` | 529 | FunctionDeclaration |
| `openCellModal` | 554 | FunctionDeclaration |
| `closeCellModal` | 576 | FunctionDeclaration |
| `setCellBlock` | 581 | FunctionDeclaration |
| `openBranchGeneratorModal` | 673 | FunctionDeclaration |
| `closeBranchGeneratorModal` | 677 | FunctionDeclaration |
| `handleApplyBranchScale` | 681 | FunctionDeclaration |
| `setPacViewMode` | 752 | FunctionDeclaration |
| `renderMonthlyProjection` | 777 | FunctionDeclaration |
| `applyMonthlyWeekAsBase` | 849 | FunctionDeclaration |
| `openSchemeConfigModal` | 866 | FunctionDeclaration |
| `closeSchemeConfigModal` | 891 | FunctionDeclaration |
| `onSchemeModeChanged` | 895 | FunctionDeclaration |
| `handleApplySchemeConfig` | 909 | FunctionDeclaration |
| `applySingleTurnPreset` | 983 | FunctionDeclaration |
| `applyDoubleTurnPreset` | 987 | FunctionDeclaration |
| `publishPacSchedule` | 991 | FunctionDeclaration |
| `openEditSlotModal` | 1037 | FunctionDeclaration |
| `closeEditSlotModal` | 1047 | FunctionDeclaration |
| `handleSaveSlot` | 1051 | FunctionDeclaration |
| `openAddSlotModal` | 1069 | FunctionDeclaration |
| `closeAddSlotModal` | 1073 | FunctionDeclaration |
| `onNewSlotDurationChanged` | 1077 | FunctionDeclaration |
| `onNewSlotTimeChanged` | 1087 | FunctionDeclaration |
| `handleAddSlot` | 1098 | FunctionDeclaration |
| `deleteSlot` | 1136 | FunctionDeclaration |
| `getActiveBlockNow` | 1152 | FunctionDeclaration |
| `updateTelemetryActiveCut` | 1198 | FunctionDeclaration |
| `applyActivePacCutToSectors` | 1216 | FunctionDeclaration |
| `setTelemetryFilter` | 1259 | FunctionDeclaration |
| `renderTelemetryList` | 1276 | FunctionDeclaration |
| `isSectorCommunity` | 1306 | `renderTelemetryList` |
| `updateSectorStatus` | 1385 | FunctionDeclaration |
| `resetAllTelemetryToNormal` | 1398 | FunctionDeclaration |
| `setAllSectorsStatus` | 1420 | FunctionDeclaration |
| `renderBlockKanban` | 1438 | FunctionDeclaration |
| `reassignSector` | 1486 | FunctionDeclaration |
| `removeSectorFromBlock` | 1510 | FunctionDeclaration |
| `openAddSectorToBlockModal` | 1522 | FunctionDeclaration |
| `closeAddSectorModal` | 1526 | FunctionDeclaration |
| `handleAddSectorToBlock` | 1530 | FunctionDeclaration |
| `normalizeSectorText` | 1562 | FunctionDeclaration |
| `getSignificantTokens` | 1578 | FunctionDeclaration |
| `levenshteinDistance` | 1584 | FunctionDeclaration |
| `computeLevenshteinSimilarity` | 1607 | FunctionDeclaration |
| `computeTokenJaccardSimilarity` | 1613 | FunctionDeclaration |
| `computeSectorSimilarity` | 1624 | FunctionDeclaration |
| `analyzeTypoClusters` | 1648 | FunctionDeclaration |
| `updateClusterBadge` | 1714 | FunctionDeclaration |
| `toggleClusteringPanel` | 1719 | FunctionDeclaration |
| `renderTypoClusters` | 1729 | FunctionDeclaration |
| `upsertSectorSafe` | 1808 | FunctionDeclaration |
| `mergeAndApproveCluster` | 1853 | FunctionDeclaration |
| `validateWithOSM` | 1992 | FunctionDeclaration |
| `renderOsmResult` | 2047 | FunctionDeclaration |
| `validateMasterSectorsWithOSM` | 2067 | FunctionDeclaration |
| `validateAllWithOSM` | 2155 | FunctionDeclaration |
| `renderCommunityTable` | 2160 | FunctionDeclaration |
| `approveCommunityLocation` | 2228 | FunctionDeclaration |
| `rejectCommunityLocation` | 2331 | FunctionDeclaration |
| `renderActiveBroadcast` | 2349 | FunctionDeclaration |
| `hideActiveBroadcast` | 2391 | FunctionDeclaration |
| `handleDismissBroadcast` | 2396 | FunctionDeclaration |
| `handleSendBroadcast` | 2415 | FunctionDeclaration |
| `testGlobalPushAlert` | 2442 | FunctionDeclaration |
| `setReportFeedFilter` | 2479 | FunctionDeclaration |
| `renderCitizenReportsFeed` | 2495 | FunctionDeclaration |
| `seedAllBarinasCatalog` | 2796 | FunctionDeclaration |
| `saveDonationConfig` | 2847 | FunctionDeclaration |
| `loadDonationUrl` | 2862 | FunctionDeclaration |
| `handleFilterMasterTypeChange` | 2891 | FunctionDeclaration |
| `getSectorCoordinates` | 2940 | FunctionDeclaration |
| `applyBasemap` | 2969 | FunctionDeclaration |
| `changeMapBasemap` | 3029 | FunctionDeclaration |
| `initBarinasMasterMap` | 3033 | FunctionDeclaration |
| `focusBarinasMapHome` | 3069 | FunctionDeclaration |
| `focusSectorOnMap` | 3074 | FunctionDeclaration |
| `updateBarinasMapMarkers` | 3095 | FunctionDeclaration |
| `toggleMapHeatmapMode` | 3204 | FunctionDeclaration |
| `handleMapBlockFilterChange` | 3219 | FunctionDeclaration |
| `initMasterFilterDropdowns` | 3224 | FunctionDeclaration |
| `handleFilterMasterMunChange` | 3249 | FunctionDeclaration |
| `handleFilterMasterChange` | 3270 | FunctionDeclaration |
| `handleSearchMasterInput` | 3278 | FunctionDeclaration |
| `setDirectoryViewMode` | 3283 | FunctionDeclaration |
| `renderGeneralSectorsMaster` | 3299 | FunctionDeclaration |
| `renderWikipediaCardsView` | 3379 | FunctionDeclaration |
| `renderDetailedTableView` | 3500 | FunctionDeclaration |
| `editSector` | 3604 | FunctionDeclaration |
| `openCreateSectorModal` | 3611 | FunctionDeclaration |
| `onclick` | 3693 | `openCreateSectorModal` |
| `closeCreateSectorModal` | 3731 | FunctionDeclaration |
| `handleModalMunicipioChange` | 3736 | FunctionDeclaration |
| `handleGeocodeForManualSector` | 3751 | FunctionDeclaration |
| `handleSaveManualSector` | 3796 | FunctionDeclaration |
| `deleteMasterSector` | 3900 | FunctionDeclaration |
| `openDbMigrationHelper` | 3958 | FunctionDeclaration |
| `closeDbMigrationHelper` | 3966 | FunctionDeclaration |
| `checkSchemaCacheStatus` | 3971 | FunctionDeclaration |
| `copyMigrationSql` | 4031 | FunctionDeclaration |
| `runClientSideLegacyMigration` | 4082 | FunctionDeclaration |

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
| `AdminPortal` | 13 | FunctionDeclaration |
| `fetchAdminData` | 74 | `AdminPortal` |
| `handleLogin` | 140 | `AdminPortal` |
| `handleLogout` | 152 | `AdminPortal` |
| `cycleCell` | 159 | `AdminPortal` |
| `handleSavePac` | 175 | `AdminPortal` |
| `handleSaveDonations` | 201 | `AdminPortal` |
| `handleAddSector` | 222 | `AdminPortal` |
| `handleRemoveSector` | 232 | `AdminPortal` |
| `handleSendBroadcast` | 240 | `AdminPortal` |
| `pingApi` | 264 | `AdminPortal` |
| `getBadgeClass` | 345 | `AdminPortal` |

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
| `findNextWindowForBlock` | 648 | Kotlin fun |

## app/src/main/java/com/example/data/model/PowerStatus.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

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
| `applyRemotePacScheduleJson` | 489 | Kotlin fun |
| `stopRealtimeSync` | 555 | Kotlin fun |

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
| `onReceive` | 16 | Kotlin fun |
| `handlePreOutageAlert` | 29 | Kotlin fun |
| `handleRestoreAlert` | 46 | Kotlin fun |
| `handleStopAlarm` | 59 | Kotlin fun |
| `handleBoot` | 68 | Kotlin fun |
| `showAlarmNotification` | 72 | Kotlin fun |
| `showRestoreNotification` | 133 | Kotlin fun |
| `handleValidationAlert` | 178 | Kotlin fun |

## app/src/main/java/com/example/notification/PacAlarmScheduler.kt

Explicación: [03-android-interfaz-y-segundo-plano.md](03-android-interfaz-y-segundo-plano.md).

| Función | Línea | Contexto |
|---|---:|---|
| `scheduleNextAlarm` | 21 | Kotlin fun |
| `cancelAlarms` | 154 | Kotlin fun |

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
| `selectSector` | 180 | Kotlin fun |
| `switchProfile` | 210 | Kotlin fun |
| `selectSectorByName` | 238 | Kotlin fun |
| `setFilter` | 247 | Kotlin fun |
| `setTab` | 251 | Kotlin fun |
| `toggleDarkMode` | 255 | Kotlin fun |
| `updateTheme` | 261 | Kotlin fun |
| `reportPowerStatus` | 281 | Kotlin fun |
| `syncPendingReportsNow` | 319 | Kotlin fun |
| `triggerPushAlertSimulation` | 336 | Kotlin fun |
| `buildPacStateCopy` | 351 | Kotlin fun |
| `setAdminOpen` | 365 | Kotlin fun |
| `updateMatrixCell` | 369 | Kotlin fun |
| `updateSlot` | 377 | Kotlin fun |
| `deleteSlot` | 385 | Kotlin fun |
| `addSlot` | 395 | Kotlin fun |
| `addSectorToBlock` | 403 | Kotlin fun |
| `removeSectorFromBlock` | 411 | Kotlin fun |
| `reassignSector` | 419 | Kotlin fun |
| `applyDoubleTurnPreset` | 427 | Kotlin fun |
| `applySingleTurnPreset` | 437 | Kotlin fun |
| `resetScheduleToDefault` | 447 | Kotlin fun |
| `deleteTurnAndRebalance` | 451 | Kotlin fun |
| `registerCommunityLocation` | 461 | Kotlin fun |
| `setOnboardingOpen` | 504 | Kotlin fun |
| `setUserLocation` | 508 | Kotlin fun |
| `publishScheduleNotification` | 550 | Kotlin fun |
| `dismissBroadcastNotice` | 571 | Kotlin fun |
| `clearUserMessage` | 575 | Kotlin fun |
| `refreshPrediction` | 579 | Kotlin fun |
| `onCleared` | 586 | Kotlin fun |

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
| `doWork` | 30 | Kotlin fun |
| `calculateMinutesUntil` | 91 | Kotlin fun |
| `enqueue` | 101 | Kotlin fun |

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
| `test modular arithmetic outage prediction engine` | 14 | Kotlin fun |

## app/src/test/java/com/example/GreetingScreenshotTest.kt

Explicación: [02-android-datos-y-motor.md](02-android-datos-y-motor.md).

| Función | Línea | Contexto |
|---|---:|---|
| `greeting_screenshot` | 25 | Kotlin fun |

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

| Función | Línea | Contexto |
|---|---:|---|
| `firestore_patch` | 21 | Python def |
| `firestore_create` | 33 | Python def |
| `to_firestore_value` | 50 | Python def |
| `to_firestore_fields` | 66 | Python def |
| `main` | 236 | Python def |

## seed_supabase.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

| Función | Línea | Contexto |
|---|---:|---|
| `supabase_upsert` | 16 | Python def |
| `main` | 162 | Python def |

## test.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.

## update_script.py

Explicación: [06-base-de-datos-configuracion-y-operacion.md](06-base-de-datos-configuracion-y-operacion.md).

No contiene funciones con nombre detectables; sus constantes, configuración o efectos de nivel superior se explican en el capítulo correspondiente.
