## Tomcatの設定

# webapps/ROOT ディレクトリが存在するなら削除しておく
if [ -d $CATALINA_BASE/webapps/ROOT ]; then
  rm -rf $CATALINA_BASE/webapps/ROOT
fi

# JMXを有効化する
# java.rmi.server.hostnameにして指定しているのはdocker-compose.yamlで指定したMayaaのサービス名(web)
CATALINA_OPTS=""
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote"
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.port=9012"
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.rmi.port=9012"
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.local.only=false"
CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=${JMX_HOSTNAME}"

# デバッグポートを有効か
CATALINA_OPTS="$CATALINA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"

echo "CATALIBA_OPTS = " $CATALINA_OPTS
