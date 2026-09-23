const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/ui/viewmodel/LuzBarinasViewModel.kt', 'utf8');

kt = kt.replace('val updateAvailable: AppUpdateInfo? = null', 'val updateAvailable: AppUpdateInfo? = null,\n    val donationUrl: String? = null');

fs.writeFileSync('app/src/main/java/com/example/ui/viewmodel/LuzBarinasViewModel.kt', kt);
