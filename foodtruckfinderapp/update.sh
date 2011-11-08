${JAVA_HOME}/bin/java -jar vendor/google/compiler.jar --compilation_level "SIMPLE_OPTIMIZATIONS" --js src/main/webapp/script/map.js > target/foodtrucklocator-1.0-SNAPSHOT/script/map.js
cp src/main/webapp/WEB-INF/web.prod.xml target/foodtrucklocator-1.0-SNAPSHOT/WEB-INF/web.xml
${JAVA_HOME}/bin/java -classpath ${APPENGINE_HOME}/lib/appengine-tools-api.jar com.google.appengine.tools.admin.AppCfg --email=${EMAIL} update target/foodtrucklocator-1.0-SNAPSHOT
