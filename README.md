# Overview
This repository contains the microservices application described, designed, and documented in the Red Hat reference architecture titled [Spring Boot Microservices on Red Hat OpenShift Container Platform 3](https://access.redhat.com/documentation/en-us/reference_architectures/2017/html/spring_boot_microservices_on_red_hat_openshift_container_platform_3/)

Overview video quickly runs through some features of this reference architecture application:

[![Spring Boot Microservices](http://img.youtube.com/vi/U9Nwjx-Glew/2.jpg)](http://www.youtube.com/watch?v=U9Nwjx-Glew)

# Build and Deployment
First, clone this repository:

````
$ git clone https://github.com/RHsyseng/spring-boot-msa-ocp.git LambdaAir
````

Change directory to the root of this project. It is assumed that from this point on, all instructions are executed from inside the *LambdaAir* directory.

````
$ cd LambdaAir
````

## Shared Storage
This reference architecture environment uses Network File System (NFS) to make storage available to all OpenShift nodes. 

Attach 2GB of storage and create a volume group for it, and two logical volumes of 1GB for each required persistent volume. For example:

````
$ sudo pvcreate /dev/vdc
$ sudo vgcreate spring-boot-ocp /dev/vdc
$ sudo lvcreate -L 1G -n zipkin spring-boot-ocp
$ sudo lvcreate -L 1G -n zuul spring-boot-ocp
````

Create a corresponding mount directory for each logical volume and mount them.

````
$ sudo mkfs.ext4 /dev/spring-boot-ocp/zipkin
$ sudo mkdir -p /mnt/zipkin/mysql
$ sudo mount /dev/spring-boot-ocp/zipkin /mnt/zipkin/mysql

$ sudo mkfs.ext4 /dev/spring-boot-ocp/zuul
$ sudo mkdir -p /mnt/zuul/volume
$ sudo mount /dev/spring-boot-ocp/zuul /mnt/zuul/volume
````

Share these mounts with all nodes by configuring the */etc/exports* file on the NFS server, and make sure to restart the NFS service before proceeding.

## OpenShift Configuration
Create an OpenShift user, optionally with the same name, to use for creating the project and deploying the application. Assuming the use of [HTPasswd](https://access.redhat.com/documentation/en-us/openshift_container_platform/3.7/html/installation_and_configuration/install-config-configuring-authentication#HTPasswdPasswordIdentityProvider) as the authentication provider:

````
$ sudo htpasswd -c /etc/origin/master/htpasswd ocpAdmin
New password: PASSWORD
Re-type new password: PASSWORD
Adding password for user ocpAdmin
````

Grant OpenShift admin and cluster admin roles to this user, so it can create persistent volumes:

````
$ sudo oadm policy add-cluster-role-to-user admin ocpAdmin
$ sudo oadm policy add-cluster-role-to-user cluster-admin ocpAdmin
````

At this point, the new OpenShift user can be used to sign in to the cluster through the master server:

````
$ oc login -u ocpAdmin -p PASSWORD --server=https://ocp-master1.xxx.example.com:8443

Login successful.
````

Create a new project to deploy this reference architecture application:

````
$ oc new-project lambdaair --display-name="Lambda Air" --description="Spring Boot Microservices on Red Hat OpenShift Container Platform 3"
Now using project "lambdaair" on server "https://ocp-master1.xxx.example.com:8443".
````

## Zipkin Deployment
Zipkin uses MySQL database for storage, which in turn requires an OpenShift persistent volume to be created. Edit *Zipkin/zipkin-mysql-pv.json* and provide a valid NFS server and path, before proceeding. Once the file has been corrected, use it to create a persistent volume:

````
$ oc create -f Zipkin/zipkin-mysql-pv.json
persistentvolume "zipkin-mysql-data" created
````

Validate that the persistent volume is available:

````
$ oc get pv
NAME                CAPACITY   ACCESSMODES   RECLAIMPOLICY   STATUS			AGE
zipkin-mysql-data   1Gi        RWO           Recycle         Available			1m
````

Once available, use the provided zipkin template to deploy both MySQL and Zipkin services:

````
$ oc new-app -f Zipkin/zipkin-mysql.yml
--> Deploying template "lambdaair/" for "Zipkin/zipkin-mysql.yml" to project lambdaair

     
     ---------
     MySQL database service, with persistent storage. For more information about using this template, including OpenShift considerations, see https://github.com/sclorg/mysql-container/blob/master/5.7/README.md.
     
     NOTE: Scaling to more than one replica is not supported. You must have persistent volumes available in your cluster to use this template.

     The following service(s) have been created in your project: zipkin-mysql.
     
            Username: zipkin
            Password: TwnDiEpoMqOGiJNb
       Database Name: zipkin
      Connection URL: mysql://zipkin-mysql:3306/
     
     For more information about using this template, including OpenShift considerations, see https://github.com/sclorg/mysql-container/blob/master/5.7/README.md.


     * With parameters:
        * Memory Limit=512Mi
        * Namespace=openshift
        * Database Service Name=zipkin-mysql
        * MySQL Connection Username=zipkin
        * MySQL Connection Password=TwnDiEpoMqOGiJNb # generated
        * MySQL root user Password=YJmmYOO3BVyX77wL # generated
        * MySQL Database Name=zipkin
        * Volume Capacity=1Gi
        * Version of MySQL Image=5.7

--> Creating resources ...
    secret "zipkin-mysql" created
    service "zipkin" created
    service "zipkin-mysql" created
    persistentvolumeclaim "zipkin-mysql" created
    configmap "zipkin-mysql-cnf" created
    configmap "zipkin-mysql-initdb" created
    deploymentconfig "zipkin" created
    deploymentconfig "zipkin-mysql" created
    route "zipkin" created
--> Success
    Run 'oc status' to view your app.
````

Note: The output above includes randomly generated passwords for the database that will be different each time. It is advisable to note down the passwords for your deployed database, in case it is later needed for troubleshooting.


You can use *oc status* to get a report, but for further details and to view the progress of the deployment, *watch* the pods as they get created and deployed:

````
$ watch oc get pods

Every 2.0s: oc get pods                                                                                                                                                             Fri Jul 21 02:04:15 2017

NAME                    READY     STATUS              RESTARTS   AGE
zipkin-1-deploy         1/1	  Running             0          2m
zipkin-1-sclgl          0/1	  Running             0          2m
zipkin-mysql-1-deploy   1/1	  Running             0          2m
zipkin-mysql-1-tv2v1    0/1	  ContainerCreating   0          1m
````

It may take a few minutes for the deployment process to complete, at which point there should be two pods in the _Running_ state:

````
$ oc get pods
NAME                   READY     STATUS    RESTARTS   AGE
zipkin-1-k0dv6         1/1       Running   0          5m
zipkin-mysql-1-g44s7   1/1       Running   0          4m
````

Once the deployment is complete, you will be able to access the *Zipkin* console. Discover its address by querying the routes:

````
$ oc get routes
NAME      HOST/PORT                             PATH       SERVICES   PORT      TERMINATION   WILDCARD
zipkin    zipkin-lambdaair.ocp.xxx.example.com             zipkin     9411                    None
````

Use the displayed URL to access the console from a browser and verify that it works correctly.

## Service Deployment
To deploy a Spring Boot service, use *Maven* to build the project, with the *fabric8:deploy* target for the *openshift* profile to deploy the built image to OpenShift. For convenience, an aggregator *pom* file has been provided at the root of the project that delegates the same Maven build to all 6 configured modules:

````
$ mvn clean fabric8:deploy -Popenshift

[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Lambda Air 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
...
...
...
[INFO] --- fabric8-maven-plugin:3.5.30:deploy (default-cli) @ aggregation ---
[WARNING] F8: No such generated manifest file /Users/bmozaffa/RedHatDrive/SysEng/Microservices/SpringBoot/SpringBootOCP/LambdaAir/target/classes/META-INF/fabric8/openshift.yml for this project so ignoring
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] Lambda Air ......................................... SUCCESS [01:33 min]
[INFO] Lambda Air ......................................... SUCCESS [02:21 min]
[INFO] Lambda Air ......................................... SUCCESS [01:25 min]
[INFO] Lambda Air ......................................... SUCCESS [01:05 min]
[INFO] Lambda Air ......................................... SUCCESS [02:20 min]
[INFO] Lambda Air ......................................... SUCCESS [01:06 min]
[INFO] Lambda Air ......................................... SUCCESS [  1.659 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 09:55 min
[INFO] Finished at: 2017-12-08T16:03:12-08:00
[INFO] Final Memory: 67M/661M
[INFO] ------------------------------------------------------------------------
````

Once all services have been built and deployed, there should be a total of 8 running pods, including the 2 Zipkin pods from before, and a new pod for each of the 6 services:

````
$ oc get pods
NAME                       READY     STATUS      RESTARTS   AGE
airports-1-72kng           1/1       Running     0          18m
airports-s2i-1-build       0/1       Completed   0          21m
flights-1-4xkfv            1/1       Running     0          15m
flights-s2i-1-build        0/1       Completed   0          16m
presentation-1-k2xlz       1/1       Running     0          10m
presentation-s2i-1-build   0/1       Completed   0          11m
sales-1-fqxjd              1/1       Running     0          7m
sales-s2i-1-build          0/1       Completed   0          8m
salesv2-1-s1wq0            1/1       Running     0          5m
salesv2-s2i-1-build        0/1       Completed   0          6m
zipkin-1-k0dv6             1/1       Running     0          1h
zipkin-mysql-1-g44s7       1/1       Running     0          1h
zuul-1-2jkj0               1/1       Running     0          1m
zuul-s2i-1-build           0/1       Completed   0          2m
````

## Flight Search
The *presentation* service also creates a [route](https://raw.githubusercontent.com/RHsyseng/spring-boot-msa-ocp/master/Presentation/src/main/fabric8/route.yml). Once again, list the routes in the OpenShift project:

````
$ oc get routes
NAME           HOST/PORT                                    PATH      SERVICES       PORT      TERMINATION   WILDCARD
presentation   presentation-lambdaair.ocp.xxx.example.com             presentation   8080                    None
zipkin         zipkin-lambdaair.ocp.xxx.example.com                   zipkin         9411                    None
````

Use the URL of the route to access the HTML application from a browser, and verify that it comes up. Search for a flight by entering values for each of the four fields. The first search may take a bit longer, so wait a few seconds for the response.

## External Configuration
The *Presentation* service configures *Hystrix* with a [thread pool size](https://github.com/RHsyseng/spring-boot-msa-ocp/blob/master/Presentation/src/main/resources/application.yml#L22) of 20 in its environment properties. Confirm this by searching the logs of the presentation pod after a flight search operation and verify that the batch size is the same:

````
$ oc logs presentation-1-k2xlz | grep batch
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 20 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 13 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 20 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 13 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 20 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 13 tickets
````

Create a new *application.yml* file that assumes a higher number of *Sales* service pods relative to *Presentation* pods:

````
$ vi application.yml
````

Enter the following values:

````yaml
hystrix:
  threadpool:
    SalesThreads:
      coreSize: 30
      maxQueueSize: 300
      queueSizeRejectionThreshold: 300
````

Create a *configmap* using the *oc* utility based on this file:

````
$ oc create configmap presentation --from-file=application.yml

configmap "presentation" created
````

Edit the *Presentation* deployment config and mount this *ConfigMap* as */deployments/config*, where it will automatically be part of the Spring Boot application classpath:

````
$ oc edit dc presentation
````

Add a new volume with an arbitrary name, such as *config-volume*, that references the previously created *configmap*. The *volumes* definition is a child of the *template spec*. Next, create a volume mount under the container to reference this volume and specify where it should be mounted. The final result is as follows, with the new lines highlighted:

````yaml
...
        resources: {}
        securityContext:
          privileged: false
        terminationMessagePath: /dev/termination-log
        volumeMounts:
        - name: config-volume
          mountPath: /deployments/config
      volumes:
        - name: config-volume
          configMap:
            name: presentation
      dnsPolicy: ClusterFirst
      restartPolicy: Always
...
````

Once the deployment config is modified and saved, OpenShift will deploy a new version of the service that will include the overriding properties. This change is persistent and pods created in the future with this new version of the deployment config will also mount the yaml file.

List the pods and note that a new pod is being created to reflect the change in the deployment config, which is the mounted file:

````
$ oc get pods
NAME                               READY     STATUS      RESTARTS   AGE
airports-1-72kng           1/1       Running     0          18m
airports-s2i-1-build       0/1       Completed   0          21m
flights-1-4xkfv            1/1       Running     0          15m
flights-s2i-1-build        0/1       Completed   0          16m
presentation-1-k2xlz       1/1       Running     0          10m
presentation-2-deploy      0/1       ContainerCreating   0          3s
presentation-s2i-1-build   0/1       Completed   0          11m
sales-1-fqxjd              1/1       Running     0          7m
sales-s2i-1-build          0/1       Completed   0          8m
salesv2-1-s1wq0            1/1       Running     0          5m
salesv2-s2i-1-build        0/1       Completed   0          6m
zipkin-1-k0dv6             1/1       Running     0          1h
zipkin-mysql-1-g44s7       1/1       Running     0          1h
zuul-1-2jkj0               1/1       Running     0          1m
zuul-s2i-1-build           0/1       Completed   0          2m
````

Wait until the second version of the pod has started in the running state. The first version will be terminated and subsequently removed:

````
$ oc get pods
NAME                       READY     STATUS      RESTARTS   AGE
...
presentation-2-pxx85       1/1       Running     0          5m
presentation-s2i-1-build   0/1       Completed   0          1h
...
````

Once this has happened, use the browser to do one or several more flight searches. Then verify the updated thread pool size by searching the logs of the new presentation pod and verify the batch size:

````
$ oc logs presentation-2-pxx85 | grep batch
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 30 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 3 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 30 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 3 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 30 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 3 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 30 tickets
... c.r.r.o.b.l.p.s.API_GatewayController    : Will price a batch of 3 tickets
````

Notice that with the mounted overriding properties, pricing happens in concurrent batches of 30 instead of 20 items now.

## A/B Testing
Copy the groovy script provided in the *Zuul* project over to the shared storage for this service:

````
$ cp Zuul/misc/ABTestingFilterBean.groovy /mnt/zuul/volume/
````

Create a persistent volume for the *Zuul* service. External groovy files placed in this location can provide dynamic routing.

````
$ oc create -f Zuul/misc/zuul-pv.json
persistentvolume "groovy" created
````

Also create a persistent volume claim:

````
$ oc create -f Zuul/misc/zuul-pvc.json
persistentvolumeclaim "groovy-claim" created
````

Verify that the claim is bound to the persistent volume:

````
$ oc get pvc
NAME           STATUS    VOLUME              CAPACITY   ACCESSMODES   AGE
groovy-claim   Bound     groovy              1Gi        RWO           7s
zipkin-mysql   Bound     zipkin-mysql-data   1Gi        RWO           2h
````

Attach the persistent volume claim to the deployment config as a directory called *groovy* on the root of the filesystem:

````
$ oc volume dc/zuul --add --name=groovy --type=persistentVolumeClaim --claim-name=groovy-claim --mount-path=/groovy
deploymentconfig "zuul" updated
[bmozaffa@middleware-master LambdaAir]$ oc get pods
NAME                       READY     STATUS              RESTARTS   AGE
airports-1-72kng           1/1       Running             0          1h
airports-s2i-1-build       0/1       Completed           0          1h
flights-1-4xkfv            1/1       Running             0          1h
flights-s2i-1-build        0/1       Completed           0          1h
presentation-2-pxx85       1/1       Running             0          32m
presentation-s2i-1-build   0/1       Completed           0          1h
sales-1-fqxjd              1/1       Running             0          1h
sales-s2i-1-build          0/1       Completed           0          1h
salesv2-1-s1wq0            1/1       Running             0          1h
salesv2-s2i-1-build        0/1       Completed           0          1h
zipkin-1-k0dv6             1/1       Running             0          2h
zipkin-mysql-1-g44s7       1/1       Running             0          2h
zuul-1-2jkj0               1/1       Running             0          1h
zuul-2-deploy              0/1       ContainerCreating   0          4s
zuul-s2i-1-build           0/1       Completed           0          1h
````

Once again, the change prompts a new deployment and terminates the original _zuul_ pod, once the new version is started up and running.

Wait until the second version of the pod reaches the running state:

````
$ oc get pods | grep zuul
zuul-2-gz7hl               1/1       Running     0          7m
zuul-s2i-1-build           0/1       Completed   0          1h
````

Return to the browser and perform one or more flight searches. Then return to the OpenShift environment and look at the log for the zuul pod.

If the IP address received from your browser ends in an odd number, the groovy script filters pricing calls and sends them to version 2 of the *sales* service instead. This will be clear in the *zuul* log:

````
$ oc logs zuul-2-gz7hl
...
... groovy.ABTestingFilterBean               : Caller IP address is 10.3.116.79
Running filter
... groovy.ABTestingFilterBean               : Caller IP address is 10.3.116.79
Running filter
````

In this case, the logs from *salesv2* will show tickets being priced with a modified algorithm:

````
$ oc logs salesv2-1-s1wq0
... c.r.r.o.b.l.sales.service.Controller     : Priced ticket at 463 with lower hop discount
... c.r.r.o.b.l.sales.service.Controller     : Priced ticket at 425 with lower hop discount
... c.r.r.o.b.l.sales.service.Controller     : Priced ticket at 407 with lower hop discount
... c.r.r.o.b.l.sales.service.Controller     : Priced ticket at 549 with lower hop discount
... c.r.r.o.b.l.sales.service.Controller     : Priced ticket at 509 with lower hop discount
... c.r.r.o.b.l.sales.service.Controller     : Priced ticket at 598 with lower hop discount
... c.r.r.o.b.l.sales.service.Controller     : Priced ticket at 610 with lower hop discount
````

If that is not the case and your IP address ends in an even number, it will still be printed but the *Running filter* statement will not appear:

````
$ oc logs zuul-2-gz7hl
...
... groovy.ABTestingFilterBean               : Caller IP address is 10.3.116.78
... groovy.ABTestingFilterBean               : Caller IP address is 10.3.116.78
... groovy.ABTestingFilterBean               : Caller IP address is 10.3.116.78
... groovy.ABTestingFilterBean               : Caller IP address is 10.3.116.78
````

In this case, you can change the filter criteria to send IP addresses with an even digit to the new version of pricing algorithm, instead of the odd ones:

````
$ vi /mnt/zuul/volume/ABTestingFilterBean.groovy
````

````groovy
...
if( lastDigit % 2 == 0 )
{
    //Even IP address will be filtered
     true
}
else
{
    //Odd IP address won't be filtered
     false
}
...
````

Deploy a new version of the *zuul* service to pick up the updated groovy script:

````
$ oc rollout latest zuul
deploymentconfig "zuul" rolled out
````

Once the new pod is running, do a flight search again and check the logs. The calls to pricing should go to the *salesv2* service now, and logs should appear as previously described.
