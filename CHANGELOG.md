[//]: # ($formatter:off$)
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [not yet released]
- add prometheus interface and configuration
- add cloud toolkit estimation method
- fix calculation of energy for intervals different to 1sec (1 Ws = 1 J)
- use double/Double instead of BigDecimal: 
  - refactor all BigDecimals to double/Double values (for slightly better performance and slightly less precise results)
- refactor MeasureMethod hierarchy
- separate jPowerMonitor jar from demo application jpowermonitor-demo.jar. See Readme for more information.
- dependency updates:
  - upgrade com.fasterxml.jackson.datatype:jackson-datatype-jsr310 to 2.17.2
  - org.junit.jupiter:junit-jupiter to 5.11.0
  - upgrade org.assertj:assertj-core to 3.26.3
  - upgrade snakeyaml to 2.3
  - upgrade junit-jupiter to 5.11.0
  - upgrade org.slf4j:slf4j-api to 2.0.16 
  - upgrade gradle to 8.10.1

## 2024-01-17 - release 1.1.2
- upgrade httpclient to 5.3
- upgrade logback to 1.4.14
- upgrade ch.qos.logback:logback-classic to 1.4.14
- upgrade com.fasterxml.jackson.datatype:jackson-datatype-jsr310 to 2.16.1
- upgrade org.assertj:assertj-core to 3.25.1
- upgrade org.jetbrains:annotations to 24.1.0
- upgrade org.junit.jupiter:junit-jupiter to 5.10.1
- upgrade org.slf4j:slf4j-api to 2.0.11
- upgrade gradle to 8.5

## 2023-11-16 - release 1.1.1
- fix mvn central name and description
- update the carbon dioxide factor in the default configuration to the latest published value for Germany (2022)

## 2023-10-19 - release 1.1.0
- Make JUnit Extension write Joule instead of Wh in the energy column of the results csv.
- Add CO2 emission output also to JUnit extension results csv.

## 2023-10-19 - release 1.0.2
- replace discontinued Open Hardware Monitor by fork Libre Hardware Monitor
- add spanish resource bundle for csv export

## 2023-10-18 - release 1.0.1
- some minor fixes:
    - adding constants
    - no infinite loop on misconfigured csv delimiter,
    - fix NaN on first measurements with zero duration.
- remove TODO from artifactory.gradle
- upgrade dependencies

## 2023-03-07 - release 1.0.0
- refactoring
- upgrade to gradle 8

## 2022-05-31
- first alpha version

