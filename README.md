# Updated branch: integrate

# DEER Parameters

The deer:IntanceMatchingOperator has two paremeters (deer:coverage, deer:maxLimit ).

**deer:coverage** can be use to set the level of Coverage of the Properties. Calulated by "propteryCount/TotalInstanceCount" 

example: deer:coverage "8.66"; 

**deer:maxLimit** will set the maximum number of Properties. 

example: deer:maxLimit "10";

# Input reguired from Ontology Operator

<source, enpointType, file>                 // <source, enpointType, url>

<source, endpoint, www.abc.com/fileOne>     

<source, restriction, www.abc.com/Person>   


<target, enpointType, file>

<target, endpoint, www.abc.com/fileTwo>

<target, restriction, www.abc.com/Actor>

# Ouput from Intance Matching Operator

The output contains 4 Jena model.

Model at index 0 contains the input from previous operator(Ontology operator).

Model at index 1 contains the ouput of LIMES that has been processed.

Model at index 2 contains the information about the result like which entities/classes has been compared through LIMES and
what were the endpoint of data sources.
example:

If data Source is File
```{
[DEER:dataSourceType, DEER:is, File]
[DEER:sourceClass, DEER:is, http://xmlns.com/foaf/0.1/Person]  # Restriction / Class / Entity
[DEER:targetClass, DEER:is, http://xmlns.com/foaf/0.1/Person]  # Restriction / Class / Entity
[DEER:sourceDataSource, DEER:is, sourceDataFile] 
[DEER:targetDataSource, DEER:is, targetDataFile] 
```

If data Source is SparqlEndpoint
Now model at index 3 and 4 contains sparql endpoints instead of data  
```{
[DEER:dataSourceType, DEER:is, sparqlEpoint]
[DEER:sourceClass, DEER:is, http://xmlns.com/foaf/0.1/Person]   # Restriction / Class / Entity
[DEER:targetClass, DEER:is, http://xmlns.com/foaf/0.1/Person]   # Restriction / Class / Entity
[DEER:sourceDataSource, DEER:is, https://es.dbpedia.org/sparql] # Now instead of dataset it only contains url, so you have to query data yourself
[DEER:targetDataSource, DEER:is, https://es.dbpedia.org/sparql] # Now instead of dataset it only contains url, so you have to query data yourself
```

Model at index 3 contains the source dataset

Model at index 4 contains the target dataset


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
