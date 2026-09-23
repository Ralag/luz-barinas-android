const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/ui/viewmodel/LuzBarinasViewModel.kt', 'utf8');

kt = kt.replace('val updateAvailable: AppUpdateInfo? = null\n)', 'val updateAvailable: AppUpdateInfo? = null,\n    val donationUrl: String? = null\n)');

const flowInsert = `        viewModelScope.launch {
            cloudSyncRepository.donationUrlFlow.collect { url ->
                _uiState.update { it.copy(donationUrl = url) }
            }
        }`;
kt = kt.replace('cloudSyncRepository.updateInfoFlow.collect { info ->', flowInsert + '\n            cloudSyncRepository.updateInfoFlow.collect { info ->');

fs.writeFileSync('app/src/main/java/com/example/ui/viewmodel/LuzBarinasViewModel.kt', kt);
