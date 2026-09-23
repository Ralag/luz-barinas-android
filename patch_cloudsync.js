const fs = require('fs');
let kt = fs.readFileSync('app/src/main/java/com/example/data/repository/CloudSyncRepository.kt', 'utf8');

const valInsert = `
    private val _donationUrlFlow = MutableStateFlow<String?>(null)
    val donationUrlFlow = _donationUrlFlow.asStateFlow()
`;
kt = kt.replace('val updateInfoFlow = _updateInfoFlow.asStateFlow()', 'val updateInfoFlow = _updateInfoFlow.asStateFlow()' + valInsert);

const configInsert = `            "donations_url" -> {
                _donationUrlFlow.value = obj["url"]?.jsonPrimitive?.content
            }
`;
kt = kt.replace('            "version" -> {', configInsert + '            "version" -> {');
fs.writeFileSync('app/src/main/java/com/example/data/repository/CloudSyncRepository.kt', kt);
