"""Run real MySQL integration tests in an isolated workspace instance.

Uses the existing MySQL 8 binaries, never the user's installed service/data.
Temporary credentials live only in memory/child environment. No secret output.
Usage: python scripts/mysql_qa.py [--serve]
The --serve option leaves a preview backend running until Ctrl+C.
"""
from pathlib import Path
import os
import secrets
import socket
import subprocess
import sys
import time

root=Path(__file__).resolve().parents[1]
binary=Path(os.environ.get('MYSQL_BIN','C:/Program Files/MySQL/MySQL Server 8.0/bin'))
data=root/('.tools/mysql-qa-'+str(int(time.time())))
data.mkdir(parents=True,exist_ok=True)
port=3307
with socket.socket() as sock:
    if sock.connect_ex(('127.0.0.1',port))==0:
        raise RuntimeError('Port 3307 is already in use; refusing to touch that server.')
if any(data.iterdir()):
    raise RuntimeError('QA data directory already exists. Choose a fresh directory; existing data is preserved.')
log=(root/'.tools/mysql-qa-process.log').open('wb')
common=[str(binary/'mysqld.exe'),'--no-defaults',f'--basedir={binary.parent}',f'--datadir={data}']
subprocess.run(common+['--initialize-insecure','--console'],stdout=log,stderr=log,check=True)
server=subprocess.Popen(common+['--console',f'--port={port}','--bind-address=127.0.0.1','--mysqlx=OFF'],stdout=log,stderr=log)
app=None
secret=''
try:
    for attempt in range(40):
        with socket.socket() as sock:
            if sock.connect_ex(('127.0.0.1',port))==0:break
        if server.poll() is not None:raise RuntimeError('MySQL failed to start; inspect .tools/mysql-qa-process.log.')
        time.sleep(.5)
    else:raise RuntimeError('MySQL startup timed out.')
    secret=secrets.token_urlsafe(36)
    mysql=[str(binary/'mysql.exe'),'--no-defaults','--host=127.0.0.1',f'--port={port}','--user=root','--protocol=TCP']
    sql=f"CREATE DATABASE sinnar_qa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER 'sinnar_qa'@'127.0.0.1' IDENTIFIED BY '{secret}'; GRANT ALL PRIVILEGES ON sinnar_qa.* TO 'sinnar_qa'@'127.0.0.1'; ALTER USER 'root'@'localhost' IDENTIFIED BY '{secret}';"
    result=subprocess.run(mysql,input=sql.encode(),capture_output=True)
    if result.returncode:raise RuntimeError('Unable to provision isolated QA database.')
    env=os.environ.copy()
    env.update(TEST_DB_URL=f'jdbc:mysql://127.0.0.1:{port}/sinnar_qa?serverTimezone=UTC',TEST_DB_USERNAME='sinnar_qa',TEST_DB_PASSWORD=secret,TEST_DB_DRIVER='com.mysql.cj.jdbc.Driver')
    maven=root/'.tools/apache-maven-3.9.11/bin/mvn.cmd'
    repo=str(root/'.tools/m2')
    status=subprocess.run([str(maven),'-B','-ntp',f'-Dmaven.repo.local={repo}','test'],cwd=root/'backend',env=env).returncode
    if status:sys.exit(status)
    if '--serve' in sys.argv:
        env.update(DB_URL=env['TEST_DB_URL'],DB_USERNAME='sinnar_qa',DB_PASSWORD=secret,ADMIN_USERNAME='previewadmin',ADMIN_PASSWORD=secrets.token_urlsafe(32),SESSION_SECURE='false',SEED_DEMO='true',SPRING_PROFILES_ACTIVE='local',PORT='9080')
        # Tests create testadmin in this disposable database; no credentials are printed.
        app=subprocess.Popen([str(maven),'-B','-ntp',f'-Dmaven.repo.local={repo}','spring-boot:run'],cwd=root/'backend',env=env)
        print('Real MySQL QA passed. Backend preview: http://localhost:9080/ (Ctrl+C stops both processes).',flush=True)
        app.wait()
finally:
    if app and app.poll() is None:app.terminate()
    cleanup_env=os.environ.copy()
    cleanup_env['MYSQL_PWD']=secret
    subprocess.run([str(binary/'mysql.exe'),'--no-defaults','--host=127.0.0.1',f'--port={port}','--user=root','--protocol=TCP'],input=b'SHUTDOWN;',env=cleanup_env,capture_output=True,timeout=15)
    if server.poll() is None:server.terminate()
    try:server.wait(timeout=10)
    except subprocess.TimeoutExpired:server.kill()
    log.close()
