XAP hot deploy
===

About
---
This tool allows to refresh business logic without any system downtime and data loss (hot deploy).
See [gigaspaces wiki] for details.

Tool will restart all processing units defined by user.

Old deployment files for specified pu will be moved to the temp folder. 
New files will be copied to the gigaspaces xap deploy folder. 
After that application will discover all processing units and restart them.
The tool supports elastic pu.

Stateful PU restart.
---
1. Tool discover all processing unit instances and identifies their space mode.
2. All backups restarted (each instance in separate thread).
3. All primaries restarted. If 'double_restart' option enabled, primaries restarted twice to return to the original state (one by one).
In the other case primaries restarted on time (each instance in separate thread).
Use 'double_restart' if it is important what the instance on which machine should be located.

Stateless PU restart.
---
1. Tool discover all processing unit instances and restart them (each instance in separate thread).

Build
---

Source files ('xap-hot-redeploy' folder) can be located anywhere on your machine.  

For build use:

``mvn clean install``
    
Note, that tests will be skipped in this case. How to build with tests see in Tests section.


Run
---

1. Copy new jar(war) files with new classes to the `xap-hot-redeploy` folder.
2. Configure options in `xap-hot-redeploy/config.properties` file.
3. Run `run.sh (run.bat)` script from xap-hot-redeploy folder.

Parameters in `config.properties` file.
---

| Option                   | Optional/required | Default value                        | Description                                                                                                                         |
|--------------------------|-------------------|--------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| GSM_HOSTS                | required          | -                                    | Hosts on which GSM are located.                                                                                                     |
| PU                       | required          | space=space.jar, web=web.war | Map with key value pairs, where key is processing unit name, value is name of file with new classes.                                |
| SSH_USER                 | required          | user                              | Name of user on remote machine.                                                                                                     |
| GS_HOME_DIR      | required          | -                                    | Path to gigaspaces directory. Don't use backslashes in path or shield them.                                                                                                     |
| LOOKUPLOCATORS      | optional          | localhost                            | Jini lookup service locators used for unicast discovery.                                                                                                                 |
| LOOKUPGROUPS             | optional          | Gigaspace default lookup group       | Jini lookup service group.                                                                                                                        |
| IDENT_PU_TIMEOUT         | required          | 60                                | Timeout to identify processing unit (in seconds).                                                                                   |
| IDENT_SPACE_MODE_TIMEOUT | required          | 60                                 | Timeout to identify space mode (in seconds).                                                                                        |
| IDENT_INSTANCES_TIMEOUT | required          | 60                                 | Timeout to identify instances (in seconds).                                                                                        |
| RESTART_TIMEOUT | required          | 60                                 | Timeout for restarting pu (in seconds).                                                                                        |
| IS_SECURED               | optional          | false                              | Set this parameter "true" if space is secured.                                                                                      |
| DOUBLE_RESTART           | optional          | false                              | Set "true" if all instances should be placed in "original" vm after redeploy. When set to "true" primary instances restarted twice. |
| LOCAL_CLUSTER           | optional          | false                              | Set "true" for local cluster mode (testing mode). |


Results
---
In case if there are no problems with hot-redeploy application you can see success message and details about restarting pu instances: 
```sh
14:51:44,392  INFO main ConfigInitializer:init:28 - Gigaspaces location: /home/user/gigaspaces-xap-premium-10.0.0-ga
14:51:44,393  INFO main ConfigInitializer:init:29 - Pu to restart: [space, cinema, mirror]
14:51:44,393  INFO main ConfigInitializer:init:30 - Locator: null
14:51:44,393  INFO main ConfigInitializer:init:31 - Lookup group: null
14:51:44,394  INFO main ConfigInitializer:init:32 - Timeout for identify pu: 60
14:51:44,394  INFO main ConfigInitializer:init:33 - Timeout for identify instances: 60
14:51:44,394  INFO main ConfigInitializer:init:34 - Timeout for identify space mode: 60
14:51:44,395  INFO main ConfigInitializer:init:35 - Timeout for restart 60
14:51:44,395  INFO main ConfigInitializer:init:36 - Secured: false
14:51:44,395  INFO main ConfigInitializer:init:37 - Double restart: false
14:51:44,395  INFO main ConfigInitializer:init:38 - GSM Hosts: [127.0.0.1]
14:51:44,395  INFO main ConfigInitializer:init:39 - User: user
14:51:44,395  INFO main ConfigInitializer:init:40 - Is local cluster: false
14:51:52,044  INFO main StatefulPuRestarter:restartAllInstances:105 - Restarting pu space with type STATEFUL
14:51:52,045  INFO pool-6-thread-1 PuInstanceRestarter:restartPUInstance:36 - restarting instance 1 on 127.0.0.1[127.0.0.1] GSC PID:9214 mode:backup...
14:52:05,085  INFO pool-6-thread-1 PuInstanceRestarter:restartPUInstance:43 - done
14:52:06,233  INFO pool-7-thread-1 PuInstanceRestarter:restartPUInstance:36 - restarting instance 1 on 127.0.0.1[127.0.0.1] GSC PID:9213 mode:primary...
14:52:21,367  INFO pool-7-thread-1 PuInstanceRestarter:restartPUInstance:43 - done
14:52:22,433  INFO main StatelessPuRestarter:restart:23 - Restarting pu cinema with type WEB
14:52:31,107  INFO main StatelessPuRestarter:restart:25 - done
14:52:32,116  INFO main StatelessPuRestarter:restart:23 - Restarting pu mirror with type MIRROR
14:52:38,929  INFO main StatelessPuRestarter:restart:25 - done
14:52:28,945  INFO main HotRedeployMain:main:17 - Hot redeploy completed successfully
```

If there are any problems during the hot-redeploy, you will see an error message and issue description:
```sh
20:11:27,861  INFO main HotRedeployMain:checkFiles:76 - Please place new files on all GSM machines and try again.
20:11:27,864  INFO main HotRedeployMain:checkFiles:77 - Hot redeploy failed
```

All details about hot-redeploy process you can see in `hot-redeploy.log` file.

Tests
---

If you want to build tool with running tests use 

```
mvn clean install -DskipTests=false
```
>PREREQUISITES for running tests:

 * run gs-agent.sh/bat
 * lookup group and locator should be set to default values
 * properties should be set in `/tool/src/test/resources/config.properties` file
 * make sure that there is no pu with name "space" deployed already
 
Rollback
---

Rollback functionality helps to avoid loosing data, if errors occurred during the redeploy (for example - broken pu file).
When some errors occurred tool search for backup GSM. If there are more than one GSM in system, they will be restarted one by one. If there is only one GSM in system, tool look for empty GSC and restart it. 
In this cases rollback finished successfully and all pus for redeploy return to them original version.

If backup GSM and empty container not found rollback failed and system state is unstable.

Rollback working example:

```
17:03:48,679  INFO main StatefulPuRestarter:restartAllInstances:105 - Restarting pu space with type STATEFUL
17:03:48,681  INFO pool-6-thread-1 PuInstanceRestarter:restartPUInstance:36 - restarting instance 1 on 127.0.0.1[127.0.0.1] GSC PID:7612 mode:backup...
17:04:49,294  INFO pool-6-thread-1 PuInstanceRestarter:restartPUInstance:43 - done
17:10:35,739  INFO main RollbackChecker:doRollback:100 - Do rollback..
17:10:35,739  INFO main RollbackChecker:doRollback:106 - There is one GSM in system. Try to find empty GSC
17:10:35,740  INFO main RollbackChecker:doRollback:109 - Restarting GSC with id 2
17:10:53,683  INFO main RollbackChecker:doRollback:119 - Rollback completed successfully
17:10:53,684  WARN main HotRedeployMain:redeploy:44 - Hot redeploy failed. Rollback successfully completed
```

>Minimal configuration for rollback working:

* At least one backup GSM in system.

or

* If n = count of primary pu instances, you should have n + 1 GSC in system.

[gigaspaces wiki]:http://wiki.gigaspaces.com/wiki/display/XAP96/Deploying+onto+the+Service+Grid#DeployingontotheServiceGrid-HotDeploy
[SSH login without password]:http://www.linuxproblem.org/art_9.html
