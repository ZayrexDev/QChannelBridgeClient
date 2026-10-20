# Response fixture provenance

These 16 JSON fixtures retain the structure of successful read-only responses from the native tencent-channel-cli 1.0.10.
All strings and nonzero numbers have been replaced with fixture values before writing to disk. Arrays are limited to two items.
Identifiers remain strings; booleans, nulls, empty collections, field names and JSON value kinds are retained.

No login credentials, real IDs, names, content, URLs or timestamps are included. Tests require no account or remote service.
Additional edge cases and unobserved write acknowledgements are constructed explicitly in StructuredResponseTest rather than represented as captured production responses.
