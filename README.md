# DEER Plugin Starter

Using `mvn clean package` in this folder will generate the plugin under
`./target/plugin-starter-${version}-plugin.jar`.
Copy the plugin into a folder named `plugins/` in the working directory from which you
want to invoke DEER and it will automatically be loaded.

In order to invoke DEER, either download it from GitHub or use our Docker image:

```bash
$  docker run -it --rm \
   -v .:/plugins/ dicegroup/deer:latest \
   java -jar deer.jar src/test/resources/configuration.ttl
```