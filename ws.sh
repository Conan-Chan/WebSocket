mvn compile
mvn exec:java -pl websocket -Dexec.mainClass="fi.iki.elonen.samples.echo.EchoSocketSample" -Dexec.args="9090"
