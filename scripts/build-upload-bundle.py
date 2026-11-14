#!/usr/bin/env python3
import secrets,json,os,subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java_home=os.environ.get('JAVA_HOME')
if not java_home: raise SystemExit('Set JAVA_HOME to JDK 17 first.')
private=root/'signing';private.mkdir(mode=0o700,exist_ok=True);private.chmod(0o700)
config=private/'upload.json';store=private/'play-upload.jks'
if not config.exists():
 if '--create-upload-key' not in __import__('sys').argv: raise SystemExit('No upload key. Use --create-upload-key to create a new private upload key.')
 assert not store.exists(),'Existing key has no matching config; stop'
 password=secrets.token_urlsafe(36)
 env=os.environ.copy();env['WDG_UPLOAD_PASSWORD']=password
 subprocess.run([str(Path(java_home)/'bin/keytool'),'-genkeypair','-keystore',str(store),'-storetype','JKS','-alias','upload','-keyalg','RSA','-keysize','4096','-validity','10000','-dname','CN=Wi-Fi Debug Guard Upload','-storepass:env','WDG_UPLOAD_PASSWORD','-keypass:env','WDG_UPLOAD_PASSWORD'],env=env,check=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
 store.chmod(0o600)
 fd=os.open(config,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
 with os.fdopen(fd,'w') as f:json.dump({'alias':'upload','password':password},f)
r=json.loads(config.read_text());env=os.environ.copy();env.update(JAVA_HOME=java_home,WDG_UPLOAD_STORE=str(store),WDG_UPLOAD_ALIAS=r['alias'],WDG_UPLOAD_PASSWORD=r['password'])
subprocess.run(['./gradlew','--no-daemon',':app:bundleRelease'],cwd=root,env=env,check=True)
print('Signed upload bundle built; upload credentials remain in ignored signing directory.')
