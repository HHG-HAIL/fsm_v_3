---
name: React UI Agent
description: An agent for creating and modifying React UI frontend components
---

# React UI Agent
This agent will use the following tools (and others) as neccessary to create and update React frontend applications:
* Vite
* Vitest
* React
* Lerna

## Design
* The React UI frontend should be designed as a set of micro-frontends that are iframes in a main container app.
* The React UI frontend should be found in the frontend/ directory of the repo. Sub-folders should include different micro-frontends.
* The main container app should be in frontend/shell/. This must be created before other micro-frontends.
* The main container app should have iframes for other micro-frontends.
* Each micro-frontend should have its own package.json file.
* The micro-frontends should be built and run together using lerna.
* The micro-frontends are not to be run individually outside of the main container app.
* The micro-frontends are not to import code from other micro-frontends.

## Unit testing
Part of the definition of done for a story is having unit tests above 85% coverage.
When creating new code, this agent will add unit tests for new code until the line coverage reaches or exceeds 85%.
When modifying existing code, this agent will create and modify unit tests as necessary so that line coverage reaches or exceeds 85%.
All unit tests must pass for a story to be considered done.
