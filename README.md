# Updated branch: inputFile

# DEER Parameters

The deer:IntanceMatchingOperator has two paremeters (deer:coverage, deer:maxLimit ).

**deer:coverage** can be use to set the level of Coverage of the Properties. Calulated by "propteryCount/TotalInstanceCount" 

example: deer:coverage "8.66"; 

**deer:maxLimit** will set the maximum number of Properties. 

example: deer:maxLimit "10";

# Input From Ontology Operator

In N-triple format

```
<http://data.nobelprize.org/resource/laureate/958> <http://www.w3.org/2002/07/owl#sameAs> <http://sparql.cwrc.ca/ontologies/cwrc#a5686049-f450-415c-9ca6-deee26e30899-b3f841f5c0>.

<http://data.nobelprize.org/resource/laureate/448> <http://www.w3.org/2002/07/owl#sameAs> <http://sparql.cwrc.ca/ontologies/cwrc#26ad3610-a0bb-4e62-8fbc-d6be9ccbbdf6-partof-327d5213ef> .
```

In Jena Model

```
[<ModelCom   
{http://data.nobelprize.org/resource/laureate/448 @http://www.w3.org/2002/07/owl#sameAs http://sparql.cwrc.ca/ontologies/cwrc#26ad3610-a0bb-4e62-8fbc-d6be9ccbbdf6-partof-327d5213ef; http://data.nobelprize.org/resource/laureate/958 @http://www.w3.org/2002/07/owl#sameAs http://sparql.cwrc.ca/ontologies/cwrc#a5686049-f450-415c-9ca6-deee26e30899-b3f841f5c0} |  
[http://data.nobelprize.org/resource/laureate/448, http://www.w3.org/2002/07/owl#sameAs, http://sparql.cwrc.ca/ontologies/cwrc#26ad3610-a0bb-4e62-8fbc-d6be9ccbbdf6-partof-327d5213ef] [http://data.nobelprize.org/resource/laureate/958, http://www.w3.org/2002/07/owl#sameAs, http://sparql.cwrc.ca/ontologies/cwrc#a5686049-f450-415c-9ca6-deee26e30899-b3f841f5c0]>]
```


# Instance Matching Operator

Using `mvn clean package` in this folder will generate the plugin under
`./target/plugin-starter-${version}-plugin.jar`.
Copy the plugin into a folder named `plugins/` in the working directory from which you
want to invoke DEER and it will automatically be loaded.

In order to invoke DEER, either download it from GitHub or use our Docker image:

```bash
docker run -it --rm \
   -v $(pwd)/plugins:/plugins -v $(pwd)/src/test/resources:/config dicegroup/deer:latest \
   /config/configuration.ttl
```

## For Windows

1. you need to create a folder `plugins` inside deer-plugin-starter directory.

2. copy the newly generated plugin under `./target/plugin-starter-${version}-plugin.jar` to `plugins` folder.

3. run the docker command from the deer-plugin-starter directory.

### Docker command for Windows cmd
```cmd
docker run -it --rm  -v %cd%/plugins:/plugins -v %cd%/src/test/resources:/config dicegroup/deer:latest /config/configuration.ttl
```
