const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/utils/AppUpdater.kt', 'utf8');

kt = kt.replace('Toast.makeText(context, "Error al instalar. Busca el APK en tu carpeta de Descargas.", Toast.LENGTH_LONG).show()', 
`android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "Error al instalar: " + e.message, Toast.LENGTH_LONG).show()
            }`);

fs.writeFileSync('app/src/main/java/com/example/utils/AppUpdater.kt', kt);
