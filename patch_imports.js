const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/MainActivity.kt', 'utf8');
const imports = `
import com.example.notification.PacAlarmPlayer
import androidx.compose.material.icons.filled.Close
`;
kt = kt.replace('import androidx.compose.material.icons.filled.Alarm', imports + 'import androidx.compose.material.icons.filled.Alarm');
kt = kt.replace('androidx.compose.material.icons.Icons.Outlined.Settings', 'androidx.compose.material.icons.outlined.Settings');
fs.writeFileSync('app/src/main/java/com/example/MainActivity.kt', kt);
