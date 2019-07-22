HMRC Access Code Service
=

[![Docker Repository on Quay](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc-access-code/status "Docker Repository on Quay")](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc-access-code)

## Overview

This is the HMRC Access Code service. It interfaces with HMRC to provide authentication components for [pttg-ip-hmrc]. 

Currently the clients of this service are [pttg-ip-hmrc] and [eue-api-hmrc-access-code-refresh].

## Find Us

* [GitHub]

## Technical Notes

The API is implemented using Spring Boot and exposes a RESTFul interface.

The endpoints are defined in `AccessCodeResource.java`

* The `/access` endpoint is used to retrieve an access code for HMRC.
* The `/refresh` endpoint is used to trigger an update of the access code.
* The `/access/{accessCode}/report` endpoint is used to report an expired access code to remove it from caching.

## Building

### ACP

This service is built by Gradle on [Drone] using [Drone yaml].

### EBSA

This service is built by Gradle on [Jenkins] using the [build Jenkinsfile].

## Infrastructure

### ACP

This service is packaged as a Docker image and stored on [Quay.io]

This service is deployed by [Drone] onto a Kubernetes cluster using its [Kubernetes configuration]

### EBSA

This service is packaged as a Docker image and stored on AWS ECR.

This service is deployed by [Jenkins] onto a Kubernetes cluster using the [deploy Jenkinsfile].

## Running Locally

Check out the project and run the command `./gradlew bootRun` which will install gradle locally, download all dependencies, build the project and run it.

The API should then be available on http://localhost:8090/access, where:
- port 8090 is defined in `application.properties` with key `server.port`
- path `/access` is defined in `AccessCodeResource.java#getAccessCode`

This service runs locally against a HSQL in-memory database.  This should suffice for running locally.

Note that this service needs collaborating service [pttg-ip-audit]. Connection details for that services can be found in `application.properties` with keys `pttg.audit.*`, which should include the default port of the service.

This service attempts to call the HMRC test service.  However it should also work against [HMRC Stub].

## Versioning

For the versions available, see the [tags on this repository].

## Authors

See the list of [contributors] who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENCE.md]
file for details.


[contributors]:                     https://github.com/UKHomeOffice/pttg-ip-hmrc-access-code/graphs/contributors
[pttg-ip-hmrc]:                     https://github.com/UKHomeOffice/pttg-ip-hmrc
[pttg-ip-audit]:                    https://github.com/UKHomeOffice/pttg-ip-audit
[eue-api-hmrc-access-code-refresh]: https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-hmrc-acces-code-refresh
[Quay.io]:                          https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc-access-code
[kubernetes configuration]:         https://github.com/UKHomeOffice/kube-pttg-ip-hmrc-access-code
[Drone]:                            https://drone.acp.homeoffice.gov.uk/UKHomeOffice/pttg-ip-hmrc-access-code
[Drone yaml]:                       .drone.yml
[Jenkins]:                          https://eue-pttg-jenkins-dtzo-kops1.service.ops.iptho.co.uk/job/build_eue_api_hmrc_access_code/ 
[build Jenkinsfile]:                https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-shared-services-toolset/browse/Jenkinsfile.pttg_ip_hmrc_access_code
[deploy Jenkinsfile]:               https://eue-pttg-jenkins-dtzo-kops1.service.ops.iptho.co.uk/job/deploy_np_dev_push_eue_api_project_tiller/
[tags on this repository]:          https://github.com/UKHomeOffice/pttg-ip-hmrc-access-code/tags
[LICENCE.md]:                       LICENCE.md
[GitHub]:                           https://github.com/orgs/UKHomeOffice/teams/pttg
[HMRC Stub]:                        https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-hmrc-stub/

