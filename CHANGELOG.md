[//]: # ($formatter:off$)
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [not yet released]
- fix mvn central name and description
- update carbon dioxide factor inf default configuration to latest published value for Germany (2022)

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

