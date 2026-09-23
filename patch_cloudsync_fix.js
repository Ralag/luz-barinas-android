const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/data/repository/CloudSyncRepository.kt', 'utf8');
if (!kt.includes('val donationUrlFlow')) {
    kt = kt.replace('val updateInfoFlow: StateFlow<com.example.ui.viewmodel.AppUpdateInfo?> = _updateInfoFlow.asStateFlow()', 'val updateInfoFlow: StateFlow<com.example.ui.viewmodel.AppUpdateInfo?> = _updateInfoFlow.asStateFlow()\n    private val _donationUrlFlow = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)\n    val donationUrlFlow = _donationUrlFlow.asStateFlow()');
}
fs.writeFileSync('app/src/main/java/com/example/data/repository/CloudSyncRepository.kt', kt);
