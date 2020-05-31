#### doctrin-task

##### Dependencies

* Java: v11.0.7
* Operating System: Ubuntu 20.04 LTS
* Chrome Driver: v83.0.4103.39
* Firefox Driver: v0.26.0

##### Build
`mvn clean install`

##### Run

###### Test 1
`mvn exec:java -Dexec.mainClass="com.doctrin.task.covid.Main"`

###### Test 2
`mvn exec:java -Dexec.mainClass="com.doctrin.task.automation.Main" -Dexec.args="chrome chrome-output.txt"`

`mvn exec:java -Dexec.mainClass="com.doctrin.task.automation.Main" -Dexec.args="firefox filefox-output.txt"`
