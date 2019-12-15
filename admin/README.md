# Admin

A Basic User Administration application written using Angular2/Angular CLI.

To make this play nice with the J2EE packaging structure, index.html is a generated file and 
all work should be done in wip.html. The gradle build target has been configured to also run 
`ng build` and copy the generated wip.html to index.html for the war file build. The idea
is to have both intellij tomcat deploment and `ng serve` usable. Additional tweaks were 
made to the application base in the html and `"deployUrl": "/admin/"` was added to the
build options in `angular.json`

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will 
automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also 
use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` 
directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
This project was generated with [Angular CLI](https://github.com/angular/angular-cli) 
version 8.3.20.
