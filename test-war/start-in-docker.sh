#!/bin/bash

# JDK7 jboss/wildfly:8.1.0.Final  Java Runtime: 1.7.0_201
# JDK7 jboss/wildfly:10.1.0.Final Java Runtime: 1.8.0_191
#FROM jboss/wildfly:10.1.0.Final
#COPY target/mayaa-1.2-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/mayaa.war

readonly WARFILE=$(find target -name "mayaa-*.war")
#export PRESERVE_JAVA_OPTS=true
JAVA_OPTS="$JAVA_OPTS -Djboss.platform.mbeanserver"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9012"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.rmi.port=9012"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.local.only=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS="$JAVA_OPTS -Djava.rmi.server.hostname=0.0.0.0"
JAVA_OPTS="$JAVA_OPTS -Xms64m -Xmx512m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true"
#JAVA_OPTS="$JAVA_OPTS -Djava.security.manager=no -Djava.security.policy=all.policy -Djava.security.debug=access"

CONFFILE=$(mktemp)
echo $CONFFILE
cat <<"CONF" > $CONFFILE
if [ "x$JBOSS_MODULES_SYSTEM_PKGS" = "x" ]; then
   JBOSS_MODULES_SYSTEM_PKGS="org.jboss.byteman"
fi

# Uncomment the following line to prevent manipulation of JVM options
# by shell scripts.
#PRESERVE_JAVA_OPTS=true

#
# Specify options to pass to the Java VM.
#
if [ "x$JAVA_OPTS" = "x" ]; then
   JAVA_OPTS="-Xms64m -Xmx512m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true"
   JAVA_OPTS="$JAVA_OPTS -Djboss.modules.system.pkgs=$JBOSS_MODULES_SYSTEM_PKGS -Djava.awt.headless=true"
else
   echo "JAVA_OPTS already set in environment; overriding default settings with values: $JAVA_OPTS"
fi

# Sample JPDA settings for remote socket debugging
JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"

# Sample JPDA settings for shared memory debugging
#JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address=jboss"

# JMXを指定する際に明示的に
JBOSS_MODULES_SYSTEM_PKGS="${JBOSS_MODULES_SYSTEM_PKGS},org.jboss.logmanager"

# Register JBoss Logmanager early at JVM startup
JBOSS_LOG_MANAGER_LIB="$(echo $JBOSS_HOME/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-*.jar)"
JAVA_OPTS+=" -Xbootclasspath/p:$JBOSS_LOG_MANAGER_LIB -Djboss.modules.system.pkgs=$JBOSS_MODULES_SYSTEM_PKGS -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

#JAVA_OPTS+=" -Djava.security.manager -Dsecmgr -Djava.security.policy=%JBOSS_HOME%/bin/server.policy"

echo JAVA_OPTS = $JAVA_OPTS
CONF

echo ${WARFILE}
echo ${JAVA_OPTS}
docker run -it --rm \
 -p 8080:8080 \
 -p 9012:9012 \
 -p 8787:8787 \
 -p 9990:9990 \
 --env JAVA_OPTS="${JAVA_OPTS}" \
 -v$(pwd)/${WARFILE}:/opt/jboss/wildfly/standalone/deployments/mayaa.war \
 -v$(pwd)/server.policy:/opt/jboss/wildfly/bin/server.policy \
 -v$CONFFILE:/opt/jboss/wildfly/bin/standalone.conf \
 jboss/wildfly:8.1.0.Final
#   \
#  /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
rm $CONFFILE


# CATALINA_BASE=/usr/local/tomcat
# CATALINA_HOME=/usr/local/tomcat
# CATALINA_TMPDIR=/usr/local/tomcat/temp
# docker run -it --rm \
#  -p 8080:8080 \
#  -p 9012:9012 \
#  --env JAVA_OPTS="${JAVA_OPTS}" \
#  -v$(pwd)/${WARFILE}:$CATALINA_HOME/webapps/ROOT.war \
#  tomcat:latest
