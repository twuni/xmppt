# XMPPt

Pronounced /Ig'zempt/.

XMPPt is a lightweight, special-purpose XMPP library.

## Features

 * Full support for XEP-0198 (Stream Management).
 * Extremely lightweight, core Java implementation. No external dependencies.

## Limitations

 * Only SASL plain authentication is supported. **Always use protection: wrap your server in TLS!**
 * Only client-to-server communication is supported (no server-to-server).
 * Capabilities negotiation (via the XMPP Capabilities stream feature) is not supported.

# License

Copyright 2014 Twuni

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
