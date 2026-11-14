# Tier-2 reference generator

This generates the astronomy test fixtures in `kollavarsham-astronomy/src/test/resources/reference/` with
[astronomy-engine](https://github.com/cosinekitty/astronomy) (MIT), an implementation independent of ours.

```bash
npm install --ignore-scripts astronomy-engine@2.1.19
```

```bash
node generate.js "$PWD/node_modules" ../../kollavarsham-astronomy/src/test/resources/reference
```

A JPL DE440 reference would be more independent, because it isn't based on VSOP87. The JPL hosts are unreachable
from the development environment, so this is a tracked follow-up.
