"""Package portable sources and release artifacts, never local credentials or caches."""
from pathlib import Path
import zipfile
import hashlib

root = Path(__file__).resolve().parents[1]
output = root.parent / 'PairStudy.zip'
excluded_dirs = {'build', 'target', '.gradle', '.idea', '.git', '.tools', 'uploads', '__pycache__'}
excluded_names = {'local.properties', 'application-local.yml', '.env'}
with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
    for file in sorted(root.rglob('*')):
        relative = file.relative_to(root)
        if not file.is_file() or any(part in excluded_dirs for part in relative.parts):
            continue
        if file.name in excluded_names or file.suffix in {'.log', '.jks', '.keystore'}:
            continue
        archive.write(file, Path('PairStudy') / relative)
with zipfile.ZipFile(output) as archive:
    bad = archive.testzip()
    if bad:
        raise RuntimeError('Corrupt archive entry: ' + bad)
    names = set(archive.namelist())
    required = {
        'PairStudy/android/gradlew.bat',
        'PairStudy/android/gradle/wrapper/gradle-wrapper.jar',
        'PairStudy/android/app/src/main/AndroidManifest.xml',
        'PairStudy/backend/pom.xml',
        'PairStudy/backend/src/main/resources/application.yml',
        'PairStudy/database/schema.sql',
        'PairStudy/README.md',
        'PairStudy/docs/VERIFICATION.md',
    }
    if not required.issubset(names):
        raise RuntimeError('Missing required entries: ' + repr(required - names))
    print('archive entries:', len(names))
print(output)
print('bytes:', output.stat().st_size)
print('SHA256:', hashlib.sha256(output.read_bytes()).hexdigest())
