# DEER Instance Matching Operator
It is sub-task of Knowledge Graph Fusion.

## Guide: How to the example
In the example folder you can find the example of DEER instance matching operator. The folder contains source data, target data
and plugin jar file. In this example we are comparing **"foaf:Person"** in **"data_nobelprize_org.nt"** with **"foaf:Person"** in **"lov_linkeddata_es_dataset_lov.nt"**.

Step 1: Clone the repo and change branch to typeDrivenWombatSimple


    git clone https://github.com/binhudakhalid/DEER-InstanceMachingOperator.git --branch instanceMatch


Step 2: 
    
    cd DEER-InstanceMachingOperator

Step 3: 
    
    cd example/

Step 4: run the instance matching operator through docker

For windows:

    docker run -it --rm -v %cd%/data:/data -v %cd%/plugins:/plugins -v %cd%/:/config dicegroup/deer:latest /config/configuration.ttl

For linux base system:   

    docker run -it --rm -v $(pwd)/data:/data -v $(pwd)/plugins:/plugins -v $(pwd)/:/config dicegroup/deer:latest /config/configuration.ttl
  
Resut

![Alt text](/screenshot/exampl1.png?raw=true "Title")

***Output:*** At the end you will get the Reificated output as a jena model.
Here we are only showing simplified output. The entity ***http://www.inf.kcl.ac.uk/staff/simonm/*** is matching with ***http://data.nobelprize.org/resource/laureate/680*** and
the confidence is ***0.7071067811865475***

    [f9069a7b-5f4e-4081-a118-26d2dc1258ab, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement] 

    [f9069a7b-5f4e-4081-a118-26d2dc1258ab, http://www.w3.org/1999/02/22-rdf-syntax-ns#subject, http://data.nobelprize.org/resource/laureate/680]

    [f9069a7b-5f4e-4081-a118-26d2dc1258ab, http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate, http://www.w3.org/2002/07/owl#sameAs] 

    [f9069a7b-5f4e-4081-a118-26d2dc1258ab, http://www.w3.org/1999/02/22-rdf-syntax-ns#object, http://www.inf.kcl.ac.uk/staff/simonm/] 

    [f9069a7b-5f4e-4081-a118-26d2dc1258ab, https://w3id.org/deer/confidence, "0.7071067811865475"] 
    
    [https://w3id.org/deer/datasetTarget, https://w3id.org/deer/path, data/lov_linkeddata_es_dataset_lov.nt]
    [https://w3id.org/deer/datasetSource, https://w3id.org/deer/path, data/data_nobelprize_org.nt]
    [https://w3id.org/deer/objectType#1, https://w3id.org/deer/is, http://www.w3.org/1999/02/22-rdf-syntax-ns#type] 
    [https://w3id.org/deer/objectType#2, https://w3id.org/deer/is, http://xmlns.com/foaf/0.1/Person] 
    [https://w3id.org/deer/subjectType#1, https://w3id.org/deer/is, http://www.w3.org/1999/02/22-rdf-syntax-ns#type]
    [https://w3id.org/deer/subjectType#2, https://w3id.org/deer/is, http://xmlns.com/foaf/0.1/Person] 

**Note**: if you are on windows please use docker for running DEER  because sometimes it has issue on windows.


# Configuration file

The DEER operator requires multiple parametes such as type, source data, target data, sourceRestriction and targetRestriction. Additional parameters are tabuSourceProperty, tabuTargetProperty and coverage, maxLimit.

![Alt text](/screenshot/configurationSample.png?raw=true "Title")

***coverage:*** You can set the coverage of each property. Calulated by "propteryCount/TotalInstanceCount"

***maxLimit:*** Set the maximum number of properties use for comparision

***type:*** The type of input data. It can file or url endpoint.

***source:*** path to data file or url of endpoint.

***target:*** path to data file or url of endpoint.

***sourceRestriction:*** set the restrictions for source data 

***targetRestriction:*** set the restrictions for targer data 

***tabuSourceProperty:*** Source tabu properties are those properties that we don't want to use them for instance matching

***tabuTargetProperty:*** Source tabu properties are those properties that we don't want to use them for instance matching


# How to build Instance Matching Operator

Using `mvn clean package` in this folder will generate the plugin under
`./target/plugin-starter-${version}-plugin.jar`.
Copy the plugin into a folder named `plugins/` in the working directory from which you
want to invoke DEER and it will automatically be loaded.
Copy the source and target data files to `data folder`.

In order to invoke DEER, either download it from GitHub or use our Docker image:

DEER requires two parametes one is ***path to configuration file*** and other is ***path to jar file*** of Instance Matching operator.

## For Linux

```bash
docker run -it --rm \
   -v $(pwd)/plugins:/plugins -v $(pwd)/src/test/resources:/config dicegroup/deer:latest \
   /config/configuration.ttl
```

If you want to give data files to DEER operator. you need to add ***$(pwd)/data:/data***
```
docker run -it --rm -v $(pwd)/data:/data -v $(pwd)/plugins:/plugins -v $(pwd)/:/config dicegroup/deer:latest /config/configuration.ttl

```

## For Windows

1. you need to create a folder `plugins` inside deer-plugin-starter directory.

2. copy the newly generated plugin under `./target/plugin-starter-${version}-plugin.jar` to `plugins` folder.

3. run the docker command from the deer-plugin-starter directory.


```cmd
docker run -it --rm  -v %cd%/plugins:/plugins -v %cd%/src/test/resources:/config dicegroup/deer:latest /config/configuration.ttl
```

