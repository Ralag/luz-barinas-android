const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/MainActivity.kt', 'utf8');
kt = kt.replace('import androidx.compose.material.icons.filled.Close', 'import androidx.compose.material.icons.filled.Close\nimport androidx.compose.material.icons.filled.Settings');
fs.writeFileSync('app/src/main/java/com/example/MainActivity.kt', kt);
