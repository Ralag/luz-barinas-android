const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/utils/AppUpdater.kt', 'utf8');

kt = kt.replace(
    'val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName)',
    'val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), apkName)'
);

kt = kt.replace(
    'setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName)',
    'setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, apkName)'
);

fs.writeFileSync('app/src/main/java/com/example/utils/AppUpdater.kt', kt);
