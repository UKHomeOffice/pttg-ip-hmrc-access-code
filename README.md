# pttg-ip-hmrc-access-code
Proving Things To Government component to produce the current HMRC access code

HMRC Access Code service
=

[![Build Status](https://drone.digital.homeoffice.gov.uk/api/badges/UKHomeOffice/pttg-ip-hmrc-access-code/status.svg)](https://drone.digital.homeoffice.gov.uk/UKHomeOffice/pttg-ip-hmrc-access-code)

[![Docker Repository on Quay](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc-access-code/status "Docker Repository on Quay")](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc-access-code)

Overview
-

This is the HMRC Access Code service. Interfaces with the HMRC to provide authentication components for [pttg-ip-hmrc]. 

The main client of this service is [pttg-ip-hmrc], plus a scheduled job [pttg-rps-scheduler].

## Find Us

* [GitHub]
* [Quay.io]

### Technical Notes

The API is implemented using Spring Boot and exposes a RESTFul interface.

* /access

### Infrastructure

This service is packaged as a Docker image and stored on [Quay.io]

This service currently runs in AWS and has an associated [kubernetes configuration]

## Building

This service is built using Gradle on Drone using [Drone yaml]

## Versioning

For the versions available, see the [tags on this repository].

## Authors

See the list of [contributors] who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENCE.md]
file for details.


[contributors]:                     https://github.com/UKHomeOffice/pttg-ip-hmrc-access-code/graphs/contributors
[pttg-ip-hmrc]:                     https://github.com/UKHomeOffice/pttg-ip-hmrc
[pttg-rps-scheduler]:               https://github.com/UKHomeOffice/pttg-rps-scheduler
[Quay.io]:                          https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc
[kubernetes configuration]:         https://github.com/UKHomeOffice/kube-pttg-ip-hmrc-access-code
[Drone yaml]:                       .drone.yml
[tags on this repository]:          https://github.com/UKHomeOffice/pttg-ip-hmrc-access-code/tags
[LICENCE.md]:                       LICENCE.md
[GitHub]:                           https://github.com/UKHomeOffice/pttg-ip-hmrc-access-code
