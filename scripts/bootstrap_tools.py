"""Download official Maven wrapper scripts and a checksum-verified local Maven for QA."""
from pathlib import Path
import hashlib
import urllib.request
import zipfile

root = Path(__file__).resolve().parents[1]
tools = root / '.tools'
tools.mkdir(exist_ok=True)
version = '3.9.11'
base = f'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/{version}/apache-maven-{version}-bin.zip'
archive = tools / 'maven.zip'
if not (tools / f'apache-maven-{version}/bin/mvn.cmd').exists():
    data = urllib.request.urlopen(base, timeout=60).read()
    checksum = urllib.request.urlopen(base + '.sha512', timeout=30).read().decode().split()[0]
    if hashlib.sha512(data).hexdigest() != checksum:
        raise RuntimeError('Maven checksum mismatch')
    archive.write_bytes(data)
    with zipfile.ZipFile(archive) as z:
        z.extractall(tools)
import io
wrapper_url = 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper-distribution/3.3.4/maven-wrapper-distribution-3.3.4-only-script.zip'
wrapper_data = urllib.request.urlopen(wrapper_url, timeout=30).read()
wrapper_checksum = urllib.request.urlopen(wrapper_url + '.sha1', timeout=30).read().decode().split()[0]
if hashlib.sha1(wrapper_data).hexdigest() != wrapper_checksum:
    raise RuntimeError('Wrapper checksum mismatch')
with zipfile.ZipFile(io.BytesIO(wrapper_data)) as wrapper_zip:
    for name in ('mvnw', 'mvnw.cmd'):
        (root / 'backend' / name).write_bytes(wrapper_zip.read(name))
wrapper = root / 'backend/.mvn/wrapper'
wrapper.mkdir(parents=True, exist_ok=True)
wrapper.joinpath('maven-wrapper.properties').write_text(
    'wrapperVersion=3.3.4\ndistributionType=only-script\n'
    f'distributionUrl={base}\n'
    f'distributionSha256Sum={hashlib.sha256(archive.read_bytes()).hexdigest()}\n', encoding='utf-8')
print('Maven 3.9.11 verified and wrapper installed.')
