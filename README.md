# banking-app

Backend REST API for a banking application

## **Usage**

The app requires postgresql database up and running
##### Launch Postgres DB
`make setup-test-database`

With the postgres database up, you can run the app that listens on port 3000

`lein run`

Test suite can be run by using

`lein test`

See doc/api.md for usage examples
## License

Copyright © 2023 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
