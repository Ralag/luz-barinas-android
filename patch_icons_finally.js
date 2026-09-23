const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/MainActivity.kt', 'utf8');
kt = kt.replace('androidx.compose.material.icons.filled.Settings', 'androidx.compose.material.icons.Icons.Default.Settings');
fs.writeFileSync('app/src/main/java/com/example/MainActivity.kt', kt);
